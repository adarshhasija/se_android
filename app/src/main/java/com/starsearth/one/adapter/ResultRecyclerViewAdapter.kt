package com.starsearth.one.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.domain.Task
import com.starsearth.one.fragments.lists.ResultListFragment


import com.starsearth.one.fragments.lists.ResultListFragment.OnResultListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_result.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnResultListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class ResultRecyclerViewAdapter(
        private val mContext: Context,
        private val mTask: Task,
        private val mValues: List<Result>,
        private val mFragment: ResultListFragment,
        private val mListener: OnResultListFragmentInteractionListener?)
    : RecyclerView.Adapter<ResultRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val result = v.tag as Result
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mFragment?.onItemClicked(mTask, result)
            //mListener?.onResultListFragmentInteraction(mTask, result)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = mValues[position]
        holder.mDateTimeView.text = Utils.formatDateTime(result.timestamp)

        if (result is ResultTyping) {
            holder.mScoreView.text = mContext.resources.getString(R.string.result) + ": " +
                                        " " + result.getScoreSummary(mContext, mTask.isPassFail, mTask.passPercentage)
        }
        else if (result is Result) {
            holder.mScoreView.text = mContext.resources.getString(R.string.result) + ": " +
                                        " " + result.items_correct.toString()
        }


        with(holder.mView) {
            tag = result
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mDateTimeView: TextView = mView.tv_date_time
        val mScoreView: TextView = mView.tv_score

        override fun toString(): String {
            return super.toString() + " '" + mDateTimeView.text + "'"
        }
    }
}
