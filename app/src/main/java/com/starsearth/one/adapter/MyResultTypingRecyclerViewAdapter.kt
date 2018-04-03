package com.starsearth.one.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.domain.ResultGestures
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.domain.Task

import com.starsearth.one.fragments.ResultTypingFragment.OnListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyResultTypingRecyclerViewAdapter(private val mTasks : List<Task>, private val mValues: ArrayList<Any>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<MyResultTypingRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layoutId = 0
        if (mValues.size > 1) {
            layoutId = R.layout.row_result_tasks_multiple
        }
        else {
            layoutId = R.layout.row_result_tasks_single
        }

        val view = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
                //.inflate(R.layout.fragment_resulttyping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = mTasks.getOrNull(position)
        holder.mItem = mValues[position]
        holder.mTitleView.text = Utils.formatStringFirstLetterCapital(task?.title)
        if (holder.mItem is ResultTyping) {
            holder.mResultView.text = (holder.mItem as ResultTyping).getScoreSummary(holder.mView.context, task?.type)
            holder.mResultSummaryView.text = (holder.mItem as ResultTyping).getExplanationSummary(holder.mView.context, task?.type)
        }
        else if (holder.mItem is ResultGestures) {
            holder.mResultView.text = (holder.mItem as ResultGestures).getScoreSummary(holder.mView.context, task?.type)
            holder.mResultSummaryView.text = (holder.mItem as ResultGestures).getExplanationSummary(holder.mView.context, task?.type)
        }

        holder.mView.setOnClickListener {
            //holder.mItem?.let { mListener?.onListFragmentInteraction(it) }
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    fun getItem(index: Int): Any {
        return mValues.get(index)
    }

    fun addItem(index: Int, item: Any) {
        mValues.add(index, item)
    }

    fun removeItem(item: Any) {
        mValues.remove(item)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView
        val mResultView: TextView
        val mResultSummaryView: TextView
        var mItem: Any? = null

        init {
            mTitleView = mView.findViewById(R.id.tv_title) as TextView
            mResultView = mView.findViewById(R.id.tv_result) as TextView
            mResultSummaryView = mView.findViewById(R.id.tv_result_summary) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mResultView.text + "'"
        }
    }
}
