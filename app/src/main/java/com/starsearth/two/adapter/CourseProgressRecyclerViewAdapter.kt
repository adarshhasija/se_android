package com.starsearth.two.adapter

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.starsearth.two.R
import com.starsearth.two.domain.Checkpoint
import com.starsearth.two.domain.Course
import com.starsearth.two.domain.Result
import com.starsearth.two.domain.Task
import com.starsearth.two.fragments.lists.CourseProgressListFragment


import com.starsearth.two.fragments.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_courseprogress.view.*
import java.util.ArrayList

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 *
 */
class CourseProgressRecyclerViewAdapter(
        private val mContext: Context,
        private val mCourse: Course,
        private val mValues: List<Any>,   //Tasks and checkpoints
        private val mResults: ArrayList<Result>,
        private val mListener: CourseProgressListFragment.OnCourseProgressListFragmentInteractionListener?)
    : RecyclerView.Adapter<CourseProgressRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            //val item = v.tag as DummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //mListener?.onCourseProgressListFragmentInteraction()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_courseprogress, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        if (item is Task) {
            holder.llCheckpoint.visibility = View.GONE
            holder.llTask.visibility = View.VISIBLE
            holder.mTaskTitleView.text = item.title
            holder.mTaskPassedView.text =
                    if (item.isPassFail && item.isPassed(mResults)) {
                        mContext.resources.getString(R.string.passed)
                    }
                    else if (item.isPassFail && item.isAttempted(mResults)) {
                        mContext.resources.getString(R.string.failed)
                    }
                    else {
                        mContext.resources.getString(R.string.not_attempted)
                    }

            holder.mCLMain.setBackgroundColor(
                    if (item.isPassFail && item.isPassed(mResults)) {
                        Color.GREEN
                    }
                    else if (item.isPassFail && item.isAttempted(mResults)) {
                        Color.RED
                    }
                    else {
                        Color.parseColor("#c0c0c0")
                    }
            )
        }
        else {
            holder.llTask.visibility = View.GONE
            holder.llCheckpoint.visibility = View.VISIBLE
            holder.mCheckpointTitle.text = (item as Checkpoint).title
            //If the previous Task is passed, set the checkpoint to cleared
            holder.mCheckpointStatus.text =
                    if ((mValues[position-1] as Task).isPassFail && (mValues[position-1] as Task).isPassed(mResults)) {
                        mContext?.resources?.getString(R.string.reached)
                    }
                    else {
                        mContext?.resources?.getString(R.string.not_reached)
                    }
            holder.mCLMain.setBackgroundColor(
                    if ((mValues[position-1] as Task).isPassFail && (mValues[position-1] as Task).isPassed(mResults)) {
                        Color.GREEN
                    }
                    else {
                        Color.parseColor("#c0c0c0")
                    }
            )

        }

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mCLMain: ConstraintLayout = mView.cl_main

        //Task
        val llTask:             LinearLayout    = mView.clTask
        val mTaskTitleView:     TextView        = mView.tvTaskTitle
        val mTaskPassedView:    TextView        = mView.tv_is_passed

        //Checkpoint
        val llCheckpoint:       LinearLayout    = mView.llCheckpoint
        val mCheckpointTitle:   TextView        = mView.tvCheckpointTitle
        val mCheckpointStatus:  TextView        = mView.tvCheckpointStatus

        override fun toString(): String {
            return super.toString() + " '" + mTaskTitleView.text + "'"
        }
    }
}
