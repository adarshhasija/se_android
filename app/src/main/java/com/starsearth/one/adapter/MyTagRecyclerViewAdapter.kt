package com.starsearth.one.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.domain.SETeachingContent
import com.starsearth.one.domain.TagListItem


import com.starsearth.one.fragments.lists.TagListFragment.OnListFragmentInteractionListener


import kotlinx.android.synthetic.main.fragment_tag.view.*

/**
 * [RecyclerView.Adapter] that can display a [TagListItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 *
 */
class MyTagRecyclerViewAdapter(
        private val mValues: ArrayList<TagListItem>,
        private val mTeachingContent: SETeachingContent,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyTagRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as TagListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onTagListItemSelected(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mContentView.text = item.name
        if (item.tcid != null) {
            holder.mView.setBackgroundColor(Color.BLUE)
        }
        else if (item.tcid == null) {
            holder.mView.setBackgroundColor(Color.WHITE)
        }

        with(holder.mView) {
            tag = item
            setOnClickListener {
                if (mValues[position].tcid != null) {
                    mValues[position].tcid = null
                    holder.mView.setBackgroundColor(Color.WHITE)
                }
                else {
                    mValues[position].tcid = mTeachingContent.id.toString()
                    holder.mView.setBackgroundColor(Color.BLUE)
                }
            }
            //setOnClickListener(mOnClickListener)
        }
    }

    fun addItem(tagListItem: TagListItem) {
        mValues.add(tagListItem)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
