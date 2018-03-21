package com.starsearth.one.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.domain.Task

import com.starsearth.one.fragments.ResultTypingFragment.OnListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyResultTypingRecyclerViewAdapter(private val mTask : Task, private val mValues: ArrayList<ResultTyping>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<MyResultTypingRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_typing_test_result, parent, false)
                //.inflate(R.layout.fragment_resulttyping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mDateCompletedView.text = Utils.formatDateTime(mValues[position].timestamp)
        holder.mMainLabelView.text = mValues[position].getScoreSummary(holder.mView.context, mTask.type)

        holder.mView.setOnClickListener {
            //holder.mItem?.let { mListener?.onListFragmentInteraction(it) }
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    fun getItem(index: Int): ResultTyping {
        return mValues.get(index)
    }

    fun addItem(index: Int, item: ResultTyping) {
        mValues.add(index, item)
    }

    fun removeItem(item: ResultTyping) {
        mValues.remove(item)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mDateCompletedView: TextView
        val mMainLabelView: TextView
        var mItem: ResultTyping? = null

        init {
            mDateCompletedView = mView.findViewById(R.id.tv_date_completed) as TextView
            mMainLabelView = mView.findViewById(R.id.tv_label_main) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mMainLabelView.text + "'"
        }
    }
}
