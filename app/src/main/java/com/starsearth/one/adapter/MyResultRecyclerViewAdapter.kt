package com.starsearth.one.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.activity.FullScreenActivity
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.ResultGestures
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.domain.Task
import com.starsearth.one.fragments.ResultListFragment

import com.starsearth.one.fragments.ResultListFragment.OnListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyResultRecyclerViewAdapter(private val mTasks : List<Task>, private val mValues: LinkedHashMap<String, Any>, private val mListener: OnListFragmentInteractionListener?, private val mFragment: ResultListFragment) : RecyclerView.Adapter<MyResultRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layoutId = 0
        //if (mValues.size > 1) { layoutId = R.layout.row_result_tasks_multiple }
        //else { layoutId = R.layout.row_result_tasks_single }
        layoutId = R.layout.row_result_tasks_single

        val view = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
                //.inflate(R.layout.fragment_resulttyping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = mTasks.getOrNull(position)
        holder.mItem = mValues.get("high_score")
        if (position == 0) {
            holder.mTitleView.text = Utils.formatStringFirstLetterCapital(task?.title)
            if (holder.mItem is ResultTyping) {
                holder.mResultView.text = (holder.mItem as ResultTyping).getScoreSummary(holder.mView.context, task?.timed!!)
                holder.mResultSummaryView.text = (holder.mItem as ResultTyping).getExplanationSummary(holder.mView.context, task?.timed)
            }
            else if (holder.mItem is ResultGestures) {
                holder.mResultView.text = (holder.mItem as ResultGestures).getScoreSummary(holder.mView.context, task?.type)
                holder.mResultSummaryView.text = (holder.mItem as ResultGestures).getExplanationSummary(holder.mView.context, task?.type)
            }
            holder.mResultView.visibility = View.VISIBLE
            holder.mResultSummaryView.visibility = View.VISIBLE
            holder.mCta.visibility = View.VISIBLE

            holder.mView.setOnClickListener {
                //holder.mItem?.let { mListener?.onResultFragmentInteraction(it) }
                holder.mItem?.let { mFragment?.onItemClicked(task, (it as Parcelable), 0) }
            }
        }

    }

    override fun getItemCount(): Int {
        return if (mValues.containsKey("high_score")) {
            1
        } else {
            0
        } //mValues.size
    }

    fun isHigScore(result: Result) : Boolean {
        var res = false
        val high_score = mValues.get("high_score")
        if (high_score == null) {
            res = true
        }
        else if (result is ResultTyping && high_score is ResultTyping) {
            if (result.words_correct > high_score.words_correct) {
                res = true
            }
        }
        else if (result is ResultGestures && high_score is ResultGestures) {
            if (result.items_correct > high_score.items_correct) {
                res = true
            }
        }

        return res
    }

    fun getItem(key: String): Any? {
        return if (mValues.containsKey(key)) {
            mValues.get(key)
        } else {
            null
        }
    }

    fun putItem(key: String, value: Any) {
        mValues.put(key, value)
    }

    fun removeItem(item: Any) {
        mValues.remove(item)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView
        val mResultView: TextView
        val mResultSummaryView: TextView
        val mCta: TextView
        val mFullScreen: TextView
        var mItem: Any? = null

        init {
            mTitleView = mView.findViewById<TextView>(R.id.tv_title) as TextView
            mResultView = mView.findViewById<TextView>(R.id.tv_result) as TextView
            mResultSummaryView = mView.findViewById<TextView>(R.id.tv_result_summary) as TextView
            mCta = mView.findViewById<TextView>(R.id.tv_cta) as TextView
            mFullScreen = mView.findViewById<TextView>(R.id.tv_full_screen) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mResultView.text + "'"
        }
    }
}
