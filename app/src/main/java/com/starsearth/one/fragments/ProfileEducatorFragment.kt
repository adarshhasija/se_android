package com.starsearth.one.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.starsearth.one.R
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.Educator
import com.starsearth.one.managers.FirebaseManager
import kotlinx.android.synthetic.main.fragment_profile_educator.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ProfileEducatorFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ProfileEducatorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileEducatorFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mContext : Context
    private var mEducator : Educator? = null
    private var listener: OnProfileEducatorFragmentInteractionListener? = null

    private val mResultValuesListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            Log.d(TAG, "***********LINE 1**************"+dataSnapshot)
            llPleaseWait?.visibility = View.GONE
            val map = dataSnapshot?.value
            if (map != null) {
                Log.d(TAG, "********* MAP NOT NULL *****************")
                for (entry in (map as HashMap<*, *>).entries) {
                        Log.d(TAG, "*********INTO FOR ***************")
                        val key = entry.key as String
                        Log.d(TAG, "***********GOT KEY*****************")
                        val value = entry.value as Map<String, Any>
                        Log.d(TAG, "***********GOT VALUE")
                        mEducator = Educator(key, value)
                        mEducator?.let {
                            Log.d(TAG, "***********EDUCATOR VALID*********"+it.type)
                            if (it.type == Educator.Type.AUTHORIZED) {
                                changeText(getString(R.string.educator_authorized_msg))
                                btnActivate?.visibility = View.VISIBLE
                            }
                            else if (it.type == Educator.Type.ACTIVE) {
                                changeText(getString(R.string.educator_active_msg))
                                btnActivate?.visibility = View.GONE
                            }
                            else if (it.type == Educator.Type.BLOCKED) {
                                changeText(getString(R.string.educator_blocked_msg))
                                btnActivate?.visibility = View.GONE
                            }
                            else {
                                changeText(getString(R.string.educator_not_authorized_msg))
                                btnActivate?.visibility = View.GONE
                            }
                        }

                }

            }
        }

        override fun onCancelled(p0: DatabaseError?) {
            llPleaseWait?.visibility = View.GONE
            changeText(getString(R.string.educator_not_authorized_msg))
            btnActivate?.visibility = View.GONE
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_educator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llPleaseWait?.visibility = View.VISIBLE
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.phoneNumber?.let {
            val firebaseManager = FirebaseManager("educators") //Since the UI has to be made visible first, this call must be made here
            val query = firebaseManager.getQueryForEducatorsByPhoneNumber(it)
            query.addListenerForSingleValueEvent(mResultValuesListener)
        }


        btnActivate?.setOnClickListener {
            if ((activity?.application as? StarsEarthApplication)?.isNetworkConnected != true) {
                val builder = (activity?.application as StarsEarthApplication).createAlertDialog(mContext)
                builder?.setTitle(resources.getString(R.string.alert))
                builder?.setMessage(resources.getString(R.string.no_internet))
                builder?.setPositiveButton(resources.getString(android.R.string.ok)) { dialogInterface, i -> dialogInterface.dismiss() }
                builder?.show()
            }
            else {

                  mEducator?.let {
                      llPleaseWait?.visibility = View.VISIBLE
                      val mDatabase = FirebaseDatabase.getInstance().reference
                      mDatabase.run {
                          this.child("educators").child(it.uid).child("type").setValue(Educator.Type.ACTIVE)
                          val currentUser = FirebaseAuth.getInstance().currentUser
                          currentUser?.let {
                              this.child("users").child(it.uid).child("educator").setValue(Educator.Type.ACTIVE)
                          }
                      }
                              ?.addOnFailureListener {
                                  llPleaseWait?.visibility = View.GONE
                                  val builder = (activity?.application as StarsEarthApplication).createAlertDialog(mContext)
                                  builder?.setTitle(resources.getString(R.string.error))
                                  builder?.setMessage(resources.getString(R.string.something_went_wrong))
                                  builder?.setPositiveButton(resources.getString(android.R.string.ok)) { dialogInterface, i -> dialogInterface.dismiss() }
                                  builder?.show()
                              }
                              ?.addOnSuccessListener {
                                  llPleaseWait?.visibility = View.GONE
                                  btnActivate?.visibility = View.GONE
                                  changeText(getString(R.string.educator_active_msg))
                              }

                  /*    mDatabase.child("educators").child(it.uid).child("type").setValue(Educator.Type.ACTIVE)

                              .addOnFailureListener {
                          llPleaseWait?.visibility = View.GONE
                          val builder = (activity?.application as StarsEarthApplication).createAlertDialog(mContext)
                          builder?.setTitle(resources.getString(R.string.error))
                          builder?.setMessage(resources.getString(R.string.something_went_wrong))
                          builder?.setPositiveButton(resources.getString(android.R.string.ok)) { dialogInterface, i -> dialogInterface.dismiss() }
                          builder?.show()
                      }.addOnSuccessListener {
                          llPleaseWait?.visibility = View.GONE
                          btnActivate?.visibility = View.GONE
                          tvStatus?.animate()
                                  ?.alpha(0f)
                                  ?.setDuration(1000)
                                  ?.setListener(object : AnimatorListenerAdapter() {
                                      override fun onAnimationEnd(animation: Animator) {
                                          tvStatus?.setText(getString(R.string.educator_active_msg))
                                          tvStatus?.animate()
                                                  ?.alpha(1f)
                                                  ?.setDuration(1000)
                                      }
                                  })
                      } */
                  }
            }

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProfileEducatorFragmentInteractionListener) {
            mContext = context
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnProfileEducatorFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    //This function changes the status text along with a fade out/fade in animation
    private fun changeText(newText : String) {
        tvStatus?.animate()
                ?.alpha(0f)
                ?.setDuration(1000)
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        tvStatus?.setText(newText)
                        tvStatus?.animate()
                                ?.alpha(1f)
                                ?.setDuration(1000)
                    }
                })
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnProfileEducatorFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onProfileEducatorFragmentInteraction(uri: Uri)
    }

    companion object {
        val TAG = "PROFILE_EDUCATOR_FRAG"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileEducatorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ProfileEducatorFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
