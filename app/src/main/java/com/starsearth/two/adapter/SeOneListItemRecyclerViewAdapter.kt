package com.starsearth.two.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.two.R
import com.starsearth.two.domain.SEOneListItem

import com.starsearth.two.fragments.lists.SeOneListFragment.OnSeOneListFragmentInteractionListener
import com.starsearth.two.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnSeOneListFragmentInteractionListener].
 *
 */
class SeOneListItemRecyclerViewAdapter(private val mValues: List<SEOneListItem>, private val mListener: OnSeOneListFragmentInteractionListener?) : RecyclerView.Adapter<SeOneListItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_se_one_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mTitleView.text = mValues[position].text1?.replace("_", " ", true)?.capitalize()

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
