package com.starsearth.one.adapter

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.domain.Course
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.fragments.CourseProgressListFragment


import com.starsearth.one.fragments.CourseProgressListFragment.OnCourseProgressListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_courseprogress.view.*
import java.util.ArrayList

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyCourseProgressRecyclerViewAdapter(
        private val mContext: Context,
        private val mCourse: Course,
        private val mValues: List<Result>,
        private val mListener: CourseProgressListFragment.OnCourseProgressListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyCourseProgressRecyclerViewAdapter.ViewHolder>() {

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
        val task = mCourse.tasks[position]
        holder.mTaskNameView.text = task.title
        holder.mTaskPassedView.text = mContext.resources.getString(R.string.not_attempted)
        if (task.isPassFail) {
            if (task.isPassed((mValues as ArrayList<Result>))) {
                holder.mTaskPassedView.text = mContext.resources.getString(R.string.passed)
                holder.mCLMain.setBackgroundColor(Color.GREEN)
            }
            else {
                holder.mTaskPassedView.text = mContext.resources.getString(R.string.failed)
                holder.mCLMain.setBackgroundColor(Color.RED)
            }
        }

        with(holder.mView) {
            tag = task
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mCLMain: ConstraintLayout = mView.cl_main
        val mTaskNameView: TextView = mView.tv_task_name
        val mTaskPassedView: TextView = mView.tv_is_passed

        override fun toString(): String {
            return super.toString() + " '" + mTaskNameView.text + "'"
        }
    }
}
