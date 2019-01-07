package com.starsearth.one.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.domain.SEOneListItem
import com.starsearth.one.fragments.lists.SeOneListFragment

import com.starsearth.one.fragments.lists.SeOneListFragment.OnSeOneListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnSeOneListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MySeOneListItemRecyclerViewAdapter(private val mValues: List<SEOneListItem>, private val mListener: OnSeOneListFragmentInteractionListener?, private val mFragment: SeOneListFragment) : RecyclerView.Adapter<MySeOneListItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_se_one_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mTitleView.text = mValues[position].text1?.capitalize()

        holder.mView.setOnClickListener {
            holder.mItem?.let {
                mListener?.onSeOneListFragmentInteraction(it, position)
            }

        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView
        val mText2View: TextView
        var mItem: SEOneListItem? = null

        init {
            mTitleView = mView.findViewById(R.id.tv_title) as TextView
            mText2View = mView.findViewById(R.id.text2) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"
        }
    }
}
