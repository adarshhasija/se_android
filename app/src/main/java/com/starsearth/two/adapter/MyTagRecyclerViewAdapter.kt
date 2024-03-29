package com.starsearth.two.adapter


import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.starsearth.two.R
import com.starsearth.two.domain.RecordItem
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
        private val mTeachingContent: SETeachingContent?,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyTagRecyclerViewAdapter.ViewHolder>(), Filterable {

    private val mOnClickListener: View.OnClickListener
    private var mIsModeMultiSelect: Boolean

    var mValuesFiltered : ArrayList<TagListItem> = ArrayList() //For search filter purposes
    init {
        mValuesFiltered = mValues
    }

    init {
        mOnClickListener = View.OnClickListener { v ->
            //val item = v.tag as TagListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //mListener?.onTagsSaveCompleted(item)
        }
        mIsModeMultiSelect = mTeachingContent != null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValuesFiltered[position] //mValues[position] //Restore this if you want to remove filtering
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
                if (mIsModeMultiSelect) {
                    if (mValuesFiltered[position].checked) {
                        mValuesFiltered[position].checked = false
                        holder.mTickIcon.visibility = View.GONE
                    }
                    else {
                        mValuesFiltered[position].checked = true
                        holder.mTickIcon.visibility = View.VISIBLE
                    }
                }
                else {
                    mListener?.onTagListItemSelected(tag as TagListItem)
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

    fun getAllFilteredItems() : ArrayList<TagListItem> {
        return mValuesFiltered
    }

    override fun getItemCount(): Int = mValuesFiltered.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mContentView: TextView = mView.content
        val mTickIcon: ImageView = mView.ivTick

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    mValuesFiltered = mValues
                }
                else {
                    val resultsList = ArrayList<TagListItem>()
                    for (row in mValues) {
                        if (row.name.contains(charSearch, true)) {
                            resultsList.add(row)
                        }
                    }
                    mValuesFiltered = resultsList
                }
                val filterResults = FilterResults()
                filterResults.values = mValuesFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mValuesFiltered = results?.values as ArrayList<TagListItem>
                notifyDataSetChanged()
            }
        }
    }
}
