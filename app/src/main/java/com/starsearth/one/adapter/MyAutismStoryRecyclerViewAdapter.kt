package com.starsearth.one.adapter

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.domain.AutismContent


import com.starsearth.one.fragments.AutismStoryFragment.OnListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_autismstory.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 *
 */
class MyAutismStoryRecyclerViewAdapter(
        private val mValues: List<Any>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyAutismStoryRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as? AutismContent
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
        val autismContent : AutismContent? = AutismContent(mValues[position] as? Map<String, Any>)
        var contentDescription = ""
        holder.mTextViewMain.visibility = View.GONE
        holder.mTextViewLine1.visibility = View.GONE
        holder.mTextViewLine2.visibility = View.GONE
        holder.mImageView.visibility = View.GONE
        autismContent?.title?.let {
            holder.mTextViewMain.text = it
            contentDescription += it
            holder.mTextViewMain.visibility = View.VISIBLE
        }
        autismContent?.textLine1?.let {
            holder.mTextViewLine1.text = it
            contentDescription += " " + it
            holder.mTextViewLine1.visibility = View.VISIBLE
        }
        autismContent?.textLine2?.let {
            holder.mTextViewLine2.text = it
            contentDescription += " " + it
            holder.mTextViewLine2.visibility = View.VISIBLE
        }
        if (contentDescription.length > 0) {
            holder.mCardView.contentDescription = contentDescription
        }

        when (autismContent?.id) {
            1 -> holder.mImageView.setImageResource(R.drawable.autism_1)
            100 -> holder.mImageView.setImageResource(R.drawable.autism_1_5)
            2 -> holder.mImageView.setImageResource(R.drawable.autism_2)
            3 -> holder.mImageView.setImageResource(R.drawable.autism_3)
            4 -> holder.mImageView.setImageResource(R.drawable.autism_4)
            5 -> holder.mImageView.setImageResource(R.drawable.autism_5)
            6 -> holder.mImageView.setImageResource(R.drawable.autism_6)
            7 -> holder.mImageView.setImageResource(R.drawable.autism_7)
            8 -> holder.mImageView.setImageResource(R.drawable.autism_8)
            9 -> holder.mImageView.setImageResource(R.drawable.autism_9)
            10 -> holder.mImageView.setImageResource(R.drawable.autism_10)
            11 -> holder.mImageView.setImageResource(R.drawable.autism_11)
            12 -> holder.mImageView.setImageResource(R.drawable.autism_12)
            13 -> holder.mImageView.setImageResource(R.drawable.autism_13)
            14 -> holder.mImageView.setImageResource(R.drawable.autism_14)
            15 -> holder.mImageView.setImageResource(R.drawable.autism_15)
            16 -> holder.mImageView.setImageResource(R.drawable.autism_16)
            17 -> holder.mImageView.setImageResource(R.drawable.autism_17)
            18 -> holder.mImageView.setImageResource(R.drawable.autism_18)
            19 -> holder.mImageView.setImageResource(R.drawable.autism_19)
            20 -> holder.mImageView.setImageResource(R.drawable.autism_20)
            21 -> holder.mImageView.setImageResource(R.drawable.autism_21)
            22 -> holder.mImageView.setImageResource(R.drawable.autism_22)
            23 -> holder.mImageView.setImageResource(R.drawable.autism_23)
            24 -> holder.mImageView.setImageResource(R.drawable.autism_24)
            25 -> holder.mImageView.setImageResource(R.drawable.autism_25)
            else -> {

            }

        }

        holder.mImageView.visibility = View.VISIBLE

        with(holder.mCardView) {
            tag = autismContent
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mCardView: CardView = mView.cvMain
        val mImageView: ImageView = mView.ivMain
        val mTextViewMain: TextView = mView.tvMain
        val mTextViewLine1: TextView = mView.tvLine1
        val mTextViewLine2: TextView = mView.tvLine2

        override fun toString(): String {
            return super.toString() + " '" + mTextViewMain.text + "'"
        }
    }
}
