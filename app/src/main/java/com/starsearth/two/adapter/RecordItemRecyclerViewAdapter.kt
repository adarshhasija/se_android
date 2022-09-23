package com.starsearth.two.adapter

import android.content.Context
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
import com.starsearth.two.Utils
import com.starsearth.two.domain.*

import com.starsearth.two.fragments.lists.RecordListFragment.OnRecordListFragmentInteractionListener
import com.starsearth.two.fragments.dummy.DummyContent.DummyItem
import java.util.*
import kotlin.collections.ArrayList

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnRecordListFragmentInteractionListener].
 *
 */
class RecordItemRecyclerViewAdapter(private val mContext: Context?, private val mValues: ArrayList<RecordItem>, private val mListener: OnRecordListFragmentInteractionListener?) : RecyclerView.Adapter<RecordItemRecyclerViewAdapter.ViewHolder>(), Filterable {


    var mValuesFiltered : ArrayList<RecordItem> = ArrayList() //For search filter purposes
    init {
        mValuesFiltered = mValues
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_record, parent, false)
                //.inflate(R.layout.fragment_mainmenuitem, parent, false)
        return ViewHolder(view)
    }

    private fun formatLatTriedTime(input: Result?): String? {
        val time = input?.timestamp
        return time?.let { Utils.formatDate(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValuesFiltered[position] //mValues[position] //Restore this if you want to remove filtering
        //val course = holder.mItem?.course
        val teachingContent = holder.mItem?.teachingContent
        val results = holder.mItem?.results

        (teachingContent as? SETeachingContent)?.let {
            holder.mTitleView.text = Utils.formatStringFirstLetterCapital(it.title)
        }
        (teachingContent as? Task)?.let {
            val type = it.type.toString()
            val extendedType =
                    if (it.type == Task.Type.SEE_AND_TYPE) {
                        //" - " +
                                it.highestResponseViewType.toString().toLowerCase().capitalize()
                    }
                    else if (!it.subType.isNullOrBlank()) {
                        //" - " +
                                it.subType.toLowerCase().capitalize()
                    }
                    else {
                        ""
                    }
            var inOrder =
                    if (it.ordered) {
                        " - " + mContext?.getString(R.string.in_order)?.toLowerCase()?.capitalize()
                    }
                    else {
                        " - " + mContext?.getString(R.string.not_in_order)?.toLowerCase()?.capitalize()
                    }
            if (extendedType.length > 0) {
                holder.mTypeView.visibility = View.VISIBLE
                holder.mTypeView.text = extendedType //type + extendedType + inOrder
            }


        /*    holder.mSlidesImageView.visibility =
                    if (it.type == Task.Type.SLIDES) {
                        View.VISIBLE
                    }
                    else {
                        View.GONE
                    }
            holder.mTapImageView.visibility =
                    if (it.type == Task.Type.TAP_SWIPE) {
                        View.VISIBLE
                    }
                    else {
                        View.GONE
                    }
            holder.mSwipeImageView.visibility =
                    if (it.type == Task.Type.TAP_SWIPE) {
                        View.VISIBLE
                    }
                    else {
                        View.GONE
                    }
            holder.mAudioImageView.visibility =
                    if (it.type == Task.Type.HEAR_AND_TYPE) {
                        View.VISIBLE
                    }
                    else {
                        View.GONE
                    }
            holder.mTypingImageView.visibility =
                    if (it.type == Task.Type.HEAR_AND_TYPE || it.type == Task.Type.SEE_AND_TYPE) {
                        View.VISIBLE
                    }
                    else {
                        View.GONE
                    }
            holder.mOrderedImageView.visibility =
                    if (it.ordered) {
                        View.VISIBLE
                    }
                    else {
                        View.GONE
                    }   */
        }
        (teachingContent as? Course)?.let {
            holder.mTypeView.text = mContext?.getString(R.string.course)?.capitalize()
        }

     /*   if (teachingContent is Task && teachingContent.timed) {
            holder.mTimedView.text = mContext?.getText(R.string.timed)
        }

        holder.mTimedView.text = if (teachingContent is Task && teachingContent.timed) {
            mContext?.getText(R.string.timed)
        } else {
            ""
        }   */

    /*    holder.mTimedImageView.visibility =
                if (teachingContent is Task && teachingContent.timed) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        holder.mTimedImageView.contentDescription =
                if (teachingContent is Task && teachingContent.timed) {
                    mContext?.getText(R.string.timed)
                } else {
                    ""
                }   */ //Uncomment this to show the timed icon

        holder.mLastTriedView.text = if (results?.isNotEmpty()!!) {
            formatLatTriedTime(results.peek())
        } else {
            ""
        }

        holder.mView.setOnClickListener {
            holder.mItem?.let { mListener?.onRecordListItemInteraction(it, position) }
        }
    }

    override fun getItemCount(): Int {
        return mValuesFiltered.size
    }

    fun getFilteredItem(index: Int): RecordItem {
        return mValuesFiltered.get(index)
    }

    fun getItem(index: Int): RecordItem {
        return mValues.get(index)
    }

    fun removeAt(position: Int) {
        mValues.removeAt(position)
    }

    fun removeAtRange(startIndex: Int, endIndex: Int) {
        mValues.subList(startIndex, endIndex).clear()
    }

    fun addItem(recordItem: RecordItem) {
        //val lastTriedMillis = recordItem.results.peek().timestamp
        val index = 0; //indexToInsert(lastTriedMillis)
        mValues.add(index, recordItem)
    }

    fun addItem(recordItem: RecordItem, index: Int) {
        if (index <= mValues.size) {
            mValues.add(index, recordItem)
        }
    }

    fun replaceItem(index: Int, recordItem: RecordItem) {
        if (index <= mValues.size) {
            mValues.set(index, recordItem)
        }
    }

    fun addItems(recordItems: List<RecordItem>, index: Int) {
        if (index <= mValues.size) {
            mValues.addAll(index, recordItems)
        }
    }

    private fun indexToInsert(timestamp: Long): Int {
        if (mValues.isEmpty()) {
            return 0
        }

        //It is less than all the existing time values. Put it at the end
        val index = binarySearh(timestamp, 0, mValues.size - 1)
        return if (index > -1) {
            index
        } else mValues.size
    }

    private fun binarySearh(value: Long, startIndex: Int, endIndex: Int): Int {
        if (startIndex <= endIndex) {
            return startIndex
        }
        var result = -1
        val middleIndex = (startIndex + endIndex) / 2
        if (value > getLastTriedMillis(middleIndex)) {
            result = binarySearh(value, startIndex, middleIndex)
        } else if (value <= getLastTriedMillis(middleIndex)) {
            result = binarySearh(value, middleIndex + 1, endIndex)
        }
        return result
    }

    private fun getLastTriedMillis(index: Int): Long {
        var timestamp: Long = 0
        val mainMenuItem = mValues.get(index)
        val lastTried = mainMenuItem.results.peek()
        lastTried?.let { timestamp = it.timestamp }
        return timestamp
    }

    public fun getTeachingContentType(inputTaskId: String): Task.Type? {
        var ret : Task.Type? = null
        for (mainMenuItem in mValues) {
            val teachingContent = mainMenuItem.teachingContent
            if (teachingContent is Course) {
                val tasks = teachingContent.tasks
                for (task in tasks) {
                    val taskId = task.uid
                    if (taskId == inputTaskId) {
                        ret = task.type
                        break
                    }
                }
            }
            else if (teachingContent is Task) {
                val taskId = teachingContent.uid
                if (taskId == inputTaskId) {
                    ret = teachingContent.type
                }
            }
        }
        return ret
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView
        val mTypeView: TextView
        //val mTimedView: TextView
        val mSlidesImageView: ImageView
        val mTapImageView: ImageView
        val mSwipeImageView: ImageView
        val mAudioImageView: ImageView
        val mTypingImageView: ImageView
        val mOrderedImageView: ImageView
        val mTimedImageView: ImageView
        val mLastTriedView: TextView
        var mItem: RecordItem? = null

        init {
            mTitleView = mView.findViewById(R.id.tv_title) as TextView
            mTypeView = mView.findViewById(R.id.tv_type_interaction) as TextView
            //mTimedView = mView.findViewById(R.id.tv_timed) as TextView
            mSlidesImageView = mView.findViewById(R.id.ivSlides) as ImageView
            mTapImageView = mView.findViewById(R.id.ivTap) as ImageView
            mSwipeImageView = mView.findViewById(R.id.ivSwipe) as ImageView
            mAudioImageView = mView.findViewById(R.id.ivAudio) as ImageView
            mTypingImageView = mView.findViewById(R.id.ivTyping) as ImageView
            mOrderedImageView = mView.findViewById(R.id.ivOrdered) as ImageView
            mTimedImageView = mView.findViewById(R.id.ivTimed) as ImageView
            mLastTriedView = mView.findViewById(R.id.tvTimestamp) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"  + " '" + mTimedImageView.contentDescription + "'" + " '" + mLastTriedView.text + "'"
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
                    val resultsList = ArrayList<RecordItem>()
                    for (row in mValues) {
                        if ((row.teachingContent as SETeachingContent).title.contains(charSearch, true)) {
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
                mValuesFiltered = results?.values as ArrayList<RecordItem>
                notifyDataSetChanged()
            }
        }
    }
}
