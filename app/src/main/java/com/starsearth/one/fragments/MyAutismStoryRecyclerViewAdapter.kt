package com.starsearth.one.fragments

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.starsearth.one.R


import com.starsearth.one.fragments.AutismStoryFragment.OnListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_autismstory.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyAutismStoryRecyclerViewAdapter(
        private val mValues: List<String>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyAutismStoryRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as DummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_autismstory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mImageView.setImageResource(R.drawable.autism_1)
        holder.mTextView.text = item

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mImageView: ImageView = mView.ivMain
        val mTextView: TextView = mView.tvMain

        override fun toString(): String {
            return super.toString() + " '" + mTextView.text + "'"
        }
    }
}
