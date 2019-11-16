package com.starsearth.one.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.starsearth.one.R
import com.starsearth.one.activity.MainActivity
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

    private val mValueListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            llPleaseWait?.visibility = View.GONE
            if (mEducator != null) {
                //At the moment, this is called from 2 places. One for uid and one for phone number. One of them will return a object.
                //Therefore mEducator will not be done for the other call.
                //Once we move to Cloud Firestore and can do OR calls we can remove this IF statement
                return
            }
            val map = dataSnapshot?.value
            if (map != null) {
                for (entry in (map as HashMap<*, *>).entries) {
                        val key = entry.key as String
                        val value = entry.value as Map<String, Any>
                        mEducator = Educator(key, value)
                        mEducator?.let {
                            if (it.status == Educator.Status.AUTHORIZED) {
                                changeText(getString(R.string.educator_authorized_msg))
                                btnActivate?.let { toggleButtonWithAnimation(it, true) }
                            }
                            else if (it.status == Educator.Status.ACTIVE) {
                                changeText(getString(R.string.educator_active_msg))
                                btnActivate?.let { toggleButtonWithAnimation(it, false) }
                            }
                            else if (it.status == Educator.Status.SUSPENDED) {
                                changeText(getString(R.string.educator_suspended_msg))
                                btnActivate?.let { toggleButtonWithAnimation(it, false) }
                            }
                            else if (it.status == Educator.Status.DEACTIVATED) {
                                //TODO: Add this at a later time
                                //changeText(getString(R.string.educator_deactivated_msg))
                                //btnActivate?.let { toggleButtonWithAnimation(it, false) }
                            }
                            else {
                                changeText(getString(R.string.educator_not_authorized_msg))
                                btnActivate?.let { toggleButtonWithAnimation(it, false) }
                            }
                        }

                }

            }
        }

        override fun onCancelled(p0: DatabaseError?) {
            llPleaseWait?.visibility = View.GONE
            changeText(getString(R.string.educator_not_authorized_msg))
            btnActivate?.let { toggleButtonWithAnimation(it, false) }
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
        currentUser?.let {
            val firebaseManager = FirebaseManager("educators") //Since the UI has to be made visible first, this call must be made here
            val queryUid = firebaseManager.getQueryForEducatorsByUserid(it.uid)
            queryUid.addListenerForSingleValueEvent(mValueListener)
            val queryPn = firebaseManager.getQueryForEducatorsByPhoneNumber(it.phoneNumber)
            queryPn.addListenerForSingleValueEvent(mValueListener)


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
                      val localScopeEducator = it //New variable as it will be used in multiple places
                      llPleaseWait?.visibility = View.VISIBLE
                      val mDatabase = FirebaseDatabase.getInstance().reference
                      mDatabase.run {
                          this.child("educators").child(localScopeEducator.uid).child("status").setValue(Educator.Status.ACTIVE) //it = mEducator
                          val currentUser = FirebaseAuth.getInstance().currentUser
                          currentUser?.let {
                              this.child("educators").child(localScopeEducator.uid).child("userid").setValue(it.uid) //setting userid is first preference over phone number. User can always change the phone number
                              this.child("users").child(it.uid).child("educator").setValue(Educator.Status.ACTIVE) //Set the record in the user profile as well
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
                                  (activity as? MainActivity)?.userPropertiesUpdated()
                              }

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

    //Will made the activate button fade in/fade out with animation
    private fun toggleButtonWithAnimation(button : Button, shouldMakeVisible : Boolean) {
        if (button.visibility == View.GONE && shouldMakeVisible) {
            button.animate()
                    ?.alpha(1f)
                    ?.setDuration(1500) //Because text takes 2 seconds to change
                    ?.setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            btnActivate?.visibility = View.VISIBLE
                        }
                    })
        }
        else if (button.visibility == View.VISIBLE && !shouldMakeVisible) {
            button.animate()
                    ?.alpha(0f)
                    ?.setDuration(1500) //Because text takes 2 seconds to change
                    ?.setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            btnActivate?.visibility = View.GONE
                        }
                    })
        }
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
        // TODO: Update argument status and name
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
