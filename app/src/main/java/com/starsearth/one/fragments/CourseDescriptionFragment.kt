package com.starsearth.one.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.starsearth.one.R
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.Course
import kotlinx.android.synthetic.main.fragment_course_description.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_COURSE = "ARG_COURSE"
private const val ARG_SHOW_TAP_TO_CONTINUE = "ARG_TAP_TO_CONTINUE"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CourseDescriptionFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CourseDescriptionFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class CourseDescriptionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var mCourse: Course
    private var mShowTapToContinue: Boolean = false
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mCourse = it.getParcelable(ARG_COURSE)
            mShowTapToContinue = it.getBoolean(ARG_SHOW_TAP_TO_CONTINUE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvCourseDescription?.text = mCourse?.description
        tvTapScreenToHearContent?.visibility =
                if ((activity?.application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvTapScreenToContinue?.visibility =
                if (mShowTapToContinue) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvTapScreenToContinue?.text =
                if ((activity?.application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                    getString(R.string.double_tap_screen_to_continue)
                }
                else {
                    getString(R.string.tap_screen_to_continue)
                }

        if (mShowTapToContinue) {
            clMain?.setOnClickListener {
                closeFragment()
            }
        }
    }

    fun closeFragment() {
        listener?.onCourseDescriptionFragmentInteraction(mCourse)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
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
        fun onCourseDescriptionFragmentInteraction(course: Course)
    }

    companion object {
        val TAG = "COURSE_DESCRIPTION_FRAGMENT"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CourseDescriptionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(course: Parcelable) =
                CourseDescriptionFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_COURSE, course)
                    }
                }

        fun newInstance(course: Parcelable, showTapToContinue: Boolean) =
                CourseDescriptionFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_COURSE, course)
                        putBoolean(ARG_SHOW_TAP_TO_CONTINUE, showTapToContinue)
                    }
                }
    }
}
