package com.starsearth.one.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.domain.MainMenuItem
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.SEBaseObject
import com.starsearth.one.fragments.MainMenuItemFragment

import com.starsearth.one.fragments.MainMenuItemFragment.OnListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyMainMenuItemRecyclerViewAdapter(private val mValues: ArrayList<MainMenuItem>, private val mListener: OnListFragmentInteractionListener?, private val mFragment: MainMenuItemFragment) : RecyclerView.Adapter<MyMainMenuItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_main, parent, false)
                //.inflate(R.layout.fragment_mainmenuitem, parent, false)
        return ViewHolder(view)
    }

    private fun formatLatTriedTime(input: Result?): String? {
        val time = input?.timestamp
        return time?.let { Utils.formatDateTime(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        //val course = holder.mItem?.course
        val teachingContent = holder.mItem?.teachingContent
        val results = holder.mItem?.results

        (teachingContent as SEBaseObject)?.title?.let { holder.mText1View.text = Utils.formatStringFirstLetterCapital(it) }

        val result = results?.peek()
        holder.mText2View.text = formatLatTriedTime(result)

        holder.mView.setOnClickListener {
            holder.mItem?.let { mFragment.listItemSelected(it) }
            //holder.mItem?.let { mListener?.onListFragmentInteraction(it) } //mListener?.onListFragmentInteraction(holder.mItem)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    fun getItem(index: Int): MainMenuItem {
        return mValues.get(index)
    }

    fun removeAt(position: Int) {
        mValues.removeAt(position)
    }

    fun addItem(mainMenuItem: MainMenuItem) {
        val lastTriedMillis = mainMenuItem.results.peek().timestamp
        val index = 0; //indexToInsert(lastTriedMillis)
        mValues.add(index, mainMenuItem)
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

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mText1View: TextView
        val mText2View: TextView
        var mItem: MainMenuItem? = null

        init {
            mText1View = mView.findViewById(R.id.text1) as TextView
            mText2View = mView.findViewById(R.id.text2) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mText1View.text + "'"  + " '" + mText2View.text + "'"
        }
    }
}
