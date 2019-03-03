package com.starsearth.one.fragments.lists

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starsearth.one.R
import com.starsearth.one.adapter.CourseProgressRecyclerViewAdapter
import com.starsearth.one.domain.Course
import com.starsearth.one.domain.Result

import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [CourseProgressListFragment.OnListFragmentInteractionListener] interface.
 */
class CourseProgressListFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1
    private var mCourse : Course? = null
    private var mResults: ArrayList<Result> = ArrayList()

    private var listener: OnCourseProgressListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            mCourse = it.getParcelable(ARG_COURSE)
            val parcelableArrayList = arguments?.getParcelableArrayList<Parcelable>(ARG_RESULTS)
            if (parcelableArrayList != null) {
                for (item in parcelableArrayList) {
                    mResults.add((item as Result))
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_courseprogress_list, container, false)

        val mTasksAndCheckpoints = ArrayList<Any>()
        mCourse?.let {
            for (task in it.tasks) {
                mTasksAndCheckpoints.add(task)
                if (it.checkpoints?.containsKey(task.id) == true) {
                    mTasksAndCheckpoints.add((mCourse?.checkpoints?.get(task.id) as Any))
                }
            }
        }


        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                mCourse?.let { adapter = CourseProgressRecyclerViewAdapter(context, it, mTasksAndCheckpoints, mResults, listener) }

            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCourseProgressListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
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
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnCourseProgressListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onCourseProgressListFragmentInteraction(item: DummyItem?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_COURSE = "course"
        const val ARG_RESULTS = "results"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(course: Course, results: ArrayList<Parcelable>?) =
                CourseProgressListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                        putParcelable(ARG_COURSE, course)
                        putParcelableArrayList(ARG_RESULTS, results)
                    }
                }
    }
}
