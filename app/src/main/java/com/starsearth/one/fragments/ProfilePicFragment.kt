package com.starsearth.one.fragments

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.starsearth.one.R
import kotlinx.android.synthetic.main.fragment_profile_educator.*
import kotlinx.android.synthetic.main.fragment_profile_pic.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_IMG_ARRAY = "img_array"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ProfilePicFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ProfilePicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfilePicFragment : Fragment() {
    private lateinit var mByteArray: ByteArray
    private var mContext: Context? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mByteArray = it.getByteArray(ARG_IMG_ARRAY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_pic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmap = BitmapFactory.decodeByteArray(mByteArray, 0, mByteArray.size)
        ivMain?.setImageBitmap(bitmap)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            mContext = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnProfilePicFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
    interface OnFragmentInteractionListener {
        fun onProfilePicFragmentInteraction()
    }

    companion object {

        val TAG = "PROFILE_PIC_FRAGMENT"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param imgByteArray The byte array of the image to be passed in.
         * @return A new instance of fragment ProfilePicFragment.
         */

        @JvmStatic
        fun newInstance(imgByteArray: ByteArray) =
                ProfilePicFragment().apply {
                    arguments = Bundle().apply {
                        putByteArray(ARG_IMG_ARRAY, imgByteArray)
                    }
                }
    }
}
