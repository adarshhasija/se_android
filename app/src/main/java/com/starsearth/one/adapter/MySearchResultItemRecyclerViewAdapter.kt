package com.starsearth.one.adapter

import android.content.Context
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.domain.TagListItem
import com.starsearth.one.domain.User


import com.starsearth.one.fragments.SearchResultItemFragment.OnListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_searchresultitem.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 *
 */
class MySearchResultItemRecyclerViewAdapter(
        private val mContext: Context,
        private val mValues: List<Parcelable>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MySearchResultItemRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Parcelable
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onSearchResultListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_searchresultitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        if (item is User) {
            holder.mTitleView.visibility = View.VISIBLE
            holder.mTitleView.text = item.name
        }
        else if (item is TagListItem) {
            holder.mTitleView.visibility = View.VISIBLE
            holder.mTitleView.text = item.name
        }

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mImgProfilePic: ImageView = mView.ivProfilePic
        val mTitleView: TextView = mView.tvTitle

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"
        }
    }
}
