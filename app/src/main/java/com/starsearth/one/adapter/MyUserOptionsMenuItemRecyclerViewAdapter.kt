package com.starsearth.one.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.domain.MoreOptionsMenuItem
import com.starsearth.one.fragments.UserOptionsMenuItemFragment

import com.starsearth.one.fragments.UserOptionsMenuItemFragment.OnMoreOptionsListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnMoreOptionsListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyUserOptionsMenuItemRecyclerViewAdapter(private val mValues: List<MoreOptionsMenuItem>, private val mListener: OnMoreOptionsListFragmentInteractionListener?, private val mFragment: UserOptionsMenuItemFragment) : RecyclerView.Adapter<MyUserOptionsMenuItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_moreoptionsmenuitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mTitleView.text = mValues[position].text1
        //holder.mContentView.text = mValues[position].content

        holder.mView.setOnClickListener {
            holder.mItem?.let { //mListener?.onMoreOptionsListFragmentInteraction(it)
                mFragment.listItemSelected(it)
            }

        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView
        val mText2View: TextView
        var mItem: MoreOptionsMenuItem? = null

        init {
            mTitleView = mView.findViewById<TextView>(R.id.tv_title) as TextView
            mText2View = mView.findViewById<TextView>(R.id.text2) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"
        }
    }
}
