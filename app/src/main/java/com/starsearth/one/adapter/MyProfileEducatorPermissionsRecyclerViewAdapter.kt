package com.starsearth.one.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.domain.Educator


import com.starsearth.one.fragments.lists.ProfileEducatorPermissionsListFragment.OnListFragmentInteractionListener

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 *
 */
class MyProfileEducatorPermissionsRecyclerViewAdapter(
        private val context: Context,
        private val educator: Educator,
        private val listTitles: ArrayList<Educator.PERMISSIONS>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyProfileEducatorPermissionsRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_profileeducatorpermissions, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemTitle = listTitles[position]
        holder.item = itemTitle

        holder.mHeading1.visibility = View.VISIBLE
        holder.mHeading1.text = itemTitle.toString()
        if (itemTitle == Educator.PERMISSIONS.TAGGING) {
            holder.mView.setBackgroundColor(Color.GRAY)
            holder.mHeading2.visibility = View.VISIBLE
            holder.mHeading2.text = context.getString(R.string.tagging_explanation_yes)
        }
        else {
            holder.mView.setBackgroundColor(Color.GRAY)
            holder.mHeading2.visibility = View.VISIBLE
            holder.mHeading2.text = context.getString(R.string.tagging_explanation_no)
        }



        with(holder.mView) {
            tag = holder.item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = listTitles.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mHeading1: TextView
        val mHeading2: TextView

        var item: Educator.PERMISSIONS? = null

        init {
            mHeading1 = mView.findViewById(R.id.tvHeading1) as TextView
            mHeading2 = mView.findViewById(R.id.tvHeading2) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mHeading1.text + "'"
        }
    }
}
