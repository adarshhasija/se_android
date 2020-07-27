package com.starsearth.two.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.two.R
import com.starsearth.two.domain.SEOneListItem
import com.starsearth.two.fragments.dummy.DummyContent.DummyItem
import com.starsearth.two.fragments.lists.SeOneListFragment.OnSeOneListFragmentInteractionListener
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnSeOneListFragmentInteractionListener].
 *
 */
class SeOneListItemRecyclerViewAdapter(private val mContext: Context, private val mValues: List<SEOneListItem>, private val mListener: OnSeOneListFragmentInteractionListener?) : RecyclerView.Adapter<SeOneListItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_se_one_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mItem = item
        holder.mTitleView.text = item.text1?.replace("_", " ", true)?.capitalize()
        val titleLowerCase = item.text1.toLowerCase(Locale.getDefault())
        if (titleLowerCase == mContext.getResources().getString(R.string.typing).toLowerCase(Locale.getDefault())
                || titleLowerCase == mContext.getResources().getString(R.string.english).toLowerCase(Locale.getDefault())
                || titleLowerCase == mContext.getResources().getString(R.string.mathematics).toLowerCase(Locale.getDefault())
                || titleLowerCase == mContext.getResources().getString(R.string.`fun`).toLowerCase(Locale.getDefault())) {
            holder.mTitleView.setTypeface(null, Typeface.BOLD)
        }

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
