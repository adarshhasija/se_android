package com.starsearth.two.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.starsearth.two.R
import com.starsearth.two.domain.SETeachingContent
import com.starsearth.two.domain.TagListItem
import com.starsearth.two.fragments.lists.TagListFragment.OnListFragmentInteractionListener
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
            //val item = v.tag as TagListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //mListener?.onTagsSaveCompleted(item)
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
        if (item.checked) {
            holder.mTickIcon.visibility = View.VISIBLE
        }
        else if (!item.checked) {
            holder.mTickIcon.visibility = View.GONE
        }

        with(holder.mView) {
            tag = item
            setOnClickListener {
                if (mValues[position].checked) {
                    mValues[position].checked = false
                    holder.mTickIcon.visibility = View.GONE
                }
                else {
                    mValues[position].checked = true
                    holder.mTickIcon.visibility = View.VISIBLE
                }
            }
            //setOnClickListener(mOnClickListener)
        }
    }

    fun addItem(tagListItem: TagListItem) {
        mValues.add(tagListItem)
    }

    fun setSelected(tagName: String) {
        for (i in 0 until mValues.size) {
            if (tagName == mValues[i].name) {
                mValues[i].checked = true
            }
        }
    }

    fun getAllItems() : ArrayList<TagListItem> {
        return mValues
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mContentView: TextView = mView.content
        val mTickIcon: ImageView = mView.ivTick

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
