package com.starsearth.one.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.domain.HelpRequest


import com.starsearth.one.fragments.lists.CoronaHelpRequestsFragment.OnListFragmentInteractionListener
import com.starsearth.one.fragments.lists.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_coronahelprequests.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class CoronaHelpRequestsRecyclerViewAdapter(
        private val mValues: ArrayList<HelpRequest>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<CoronaHelpRequestsRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as HelpRequest
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onCoronaHelpListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_coronahelprequests, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mNameView.text =
                if (!item.guestName.isNullOrBlank()) {
                    item.guestName
                } else {
                    item.name
                }
        holder.mHelpNeededView.text = item.request

        if (item.status == "COMPLETE") {
            holder.mView.setBackgroundColor(Color.GREEN)
        }

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    fun addItem(helpRequestItem: HelpRequest) {
        mValues.add(helpRequestItem)
    }

    fun removeItem(helpRequestItem: HelpRequest) {
        mValues.remove(helpRequestItem)
    }

    fun removeAllItems() {
        mValues.clear()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView = mView.name
        val mHelpNeededView: TextView = mView.helpNeeded

        override fun toString(): String {
            return super.toString() + " '" + mNameView.text + "'"
        }
    }
}
