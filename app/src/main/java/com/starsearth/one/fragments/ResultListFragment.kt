package com.starsearth.one.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starsearth.one.R
import com.starsearth.one.adapter.ResultRecyclerViewAdapter
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.Task

import com.starsearth.one.fragments.dummy.DummyContent
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem
import java.util.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ResultListFragment.OnResultListFragmentInteractionListener] interface.
 */
class ResultListFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1
    private lateinit var mTask : Task
    private var mResults = ArrayList<Result>()

    private var listener: OnResultListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            mTask = it.getParcelable<Task>(ARG_TASK)
            mResults.addAll(it.getParcelableArrayList(ARG_RESULTS_ARRAY))
        }
    }

    override fun onStart() {
        super.onStart()
        Collections.reverse(mResults);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_result_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.addItemDecoration(DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL))
                adapter = ResultRecyclerViewAdapter(context, mTask, mResults, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnResultListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTaskDetailListFragmentListener")
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
    interface OnResultListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onResultListFragmentInteraction(item: Result?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_RESULTS_ARRAY = "results"
        const val ARG_TASK = "task"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(task: Task, results: ArrayList<Result>) =
                ResultListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_TASK, task)
                        putParcelableArrayList(ARG_RESULTS_ARRAY, results)
                    }
                }
    }
}
