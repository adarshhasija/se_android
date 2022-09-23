package com.starsearth.two.adapter

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.two.R
import com.starsearth.two.Utils
import com.starsearth.two.domain.SETeachingContent
import com.starsearth.two.domain.Task


import com.starsearth.two.fragments.lists.EducatorContentFragment.OnListFragmentInteractionListener
import com.starsearth.two.fragments.lists.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_educatorcontent.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 *
 */
class EducatorContentRecyclerViewAdapter(
        private val mContext: Context,
        private val mValues: ArrayList<SETeachingContent>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<EducatorContentRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as SETeachingContent
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //mListener?.onEducatorContentListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_educatorcontent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val teachingContent = mValues[position]
        holder.mItem = teachingContent

        (teachingContent as? SETeachingContent)?.let {
            holder.mTitleView.text = Utils.formatStringFirstLetterCapital(it.title)
        }
        (teachingContent as? Task)?.let {
            val type = it.type.toString()
            val extendedType =
                    if (it.type == Task.Type.SEE_AND_TYPE) {
                        " - " + it.highestResponseViewType.toString().toLowerCase(Locale.getDefault()).capitalize()
                    }
                    else if (!it.subType.isNullOrBlank()) {
                        " - " + it.subType.toLowerCase(Locale.getDefault()).capitalize()
                    }
                    else {
                        ""
                    }
            var inOrder =
                    if (it.ordered) {
                        " - " + mContext?.getString(R.string.in_order)?.toLowerCase(Locale.getDefault())?.capitalize()
                    }
                    else {
                        " - " + mContext?.getString(R.string.not_in_order)?.toLowerCase(Locale.getDefault())?.capitalize()
                    }
            holder.mTypeInteractionView.text = type + extendedType + inOrder
        }

        holder.mTimedView.text =
                if (teachingContent is Task && teachingContent.timed) {
                    mContext?.getText(R.string.timed)
                } else {
                    ""
                }

        holder.mViewsView.text =
                if (teachingContent is Task && teachingContent.views != null && teachingContent.views.toInt() > 0) {
                    "Times Viewed: " + teachingContent.views
                } else {
                    ""
                }


        holder.mView.setBackgroundColor(Color.LTGRAY)
        with(holder.mView) {
            tag = teachingContent
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    fun addItem(item : SETeachingContent) {
        mValues.add(item)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView = mView.tvTitle
        val mTypeInteractionView: TextView = mView.tvTypeInteraction
        val mTimedView: TextView = mView.tvTimed
        val mViewsView: TextView = mView.tvViews
        var mItem: SETeachingContent? = null

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"
        }
    }
}
