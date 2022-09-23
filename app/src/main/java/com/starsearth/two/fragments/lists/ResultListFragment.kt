package com.starsearth.two.fragments.lists

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.starsearth.two.R
import com.starsearth.two.adapter.ResultRecyclerViewAdapter
import com.starsearth.two.domain.Result
import com.starsearth.two.domain.ResultTyping
import com.starsearth.two.domain.Task
import kotlinx.android.synthetic.main.fragment_records_list.*

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ResultListFragment.OnResultListFragmentInteractionListener] interface.
 */
class ResultListFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1
    private var mDatabaseResultsReference: DatabaseReference? = null
    private lateinit var mTask : Task
    private var mResults = ArrayList<Result>()

    private var listener: OnResultListFragmentInteractionListener? = null

    private val mResultValuesListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val adapter = (list.adapter as ResultRecyclerViewAdapter)
            val map = snapshot?.value
            if (map != null) {
                val results = ArrayList<Result>()
                for (entry in (map as HashMap<*, *>).entries) {
                    val value = entry.value as Map<String, Any>
                    var newResult = Result(value)
                    if (mTask.uid != newResult.task_id) {
                        continue
                    }
                    if (mTask.type == Task.Type.SEE_AND_TYPE) {
                        newResult = ResultTyping(value)
                    }
                    results.add(newResult)
                }

                Collections.reverse(results) //Want to see it in reverse chronological
                adapter.addAll(results)
                adapter.notifyDataSetChanged()
            }
            progressBar?.visibility = View.GONE
            list?.visibility = View.VISIBLE
        }

        override fun onCancelled(error: DatabaseError) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            progressBar?.visibility = View.GONE
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            it.getParcelableArrayList<Result>(ARG_RESULTS_ARRAY)?.let {
                mResults.addAll(it)
                Collections.reverse(mResults)
            }
        }
        arguments?.getParcelable<Task>(ARG_TASK)?.let {
            mTask = it
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_result_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(
                        context
                    )
                    else -> GridLayoutManager(
                        context,
                        columnCount
                    )
                }
                view.addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL
                    )
                )
                adapter = ResultRecyclerViewAdapter(context, mTask, mResults,listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mResults.isEmpty()) {
            //Only call from FirebaseManager if there are no results passed in
            FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
        }
    }

    private fun setupResultsListener(currentUser: FirebaseUser) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("results")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.orderByChild("userId")?.equalTo(currentUser.uid)
        query?.addListenerForSingleValueEvent(mResultValuesListener)
        progressBar?.visibility = View.VISIBLE
        list?.visibility = View.GONE
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

        fun onResultListFragmentInteraction(task: Task?, result: Result?)
    }

    companion object {
        val TAG = "RESULT_LIST_FRAGMENT"

        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_RESULTS_ARRAY = "results"
        const val ARG_TASK = "task"


        @JvmStatic
        fun newInstance(task: Task) =
                ResultListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_TASK, task)
                    }
                }

        fun newInstance(task: Task, results: ArrayList<Result>) =
                ResultListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_TASK, task)
                        putParcelableArrayList(ARG_RESULTS_ARRAY, results)
                    }
                }
    }
}
