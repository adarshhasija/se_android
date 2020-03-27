package com.starsearth.one.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.starsearth.one.R
import com.starsearth.one.adapter.MyAutismStoryRecyclerViewAdapter
import com.starsearth.one.domain.Task

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [AutismStoryFragment.OnListFragmentInteractionListener] interface.
 */
class AutismStoryFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1
    private lateinit var mTask : Task
    private var mAdapter: MyAutismStoryRecyclerViewAdapter? = null

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mTask = it.getParcelable(ARG_TASK)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_autismstory_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    else -> GridLayoutManager(context, columnCount)
                }
                mAdapter = MyAutismStoryRecyclerViewAdapter(mTask.content as List<Any>, listener)
                adapter = mAdapter
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newViewsCount : String = if (mTask.views != null) {
            (mTask.views.toInt() + 1).toString()
        } else {
            "1" //views was null. This is the first view.
        }
        val mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("teachingcontent").child(mTask.uid).child("views").setValue(newViewsCount);


        val contentList = mTask.content as List<Map<String, Any>>
        for (content in contentList) {
            val hasImage = content.containsKey("hasImage") && (content.get("hasImage") as Boolean)
            if (hasImage) {
                val contentId = content["id"].toString()
                var picRef = FirebaseStorage.getInstance().reference.child("images/tc_"+contentId+".jpg")
                val ONE_MEGABYTE: Long = 1024 * 1024
                picRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                    mAdapter?.addImage(contentId, it)
                }.addOnFailureListener {
                    // Handle any errors
                    mAdapter?.addImage(contentId, null)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
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
    interface OnListFragmentInteractionListener {
        //fun onListFragmentInteraction(item: DummyItem?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val TAG = "AUTISM_STORY_FRAGMENT"
        const val ARG_TASK = "task"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(task: Task) =
                AutismStoryFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_TASK, task)
                    }
                }
    }
}
