package com.starsearth.one.adapter

import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.Task
import com.starsearth.one.fragments.TaskDetailListFragment

import com.starsearth.one.fragments.TaskDetailListFragment.OnTaskDetailListFragmentListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem
import kotlin.collections.LinkedHashMap

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnTaskDetailListFragmentListener].
 *
 */
class TaskDetailRecyclerViewAdapter(private val mTasks : List<Task>, private val mValues: LinkedHashMap<String, Any>, private val mListener: OnTaskDetailListFragmentListener?, private val mFragment: TaskDetailListFragment) : RecyclerView.Adapter<TaskDetailRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layoutId = R.layout.task_detail_list_item

        val view = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
                //.inflate(R.layout.fragment_resulttyping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //If we are passing in course, course has multiple tasks
        val task = mTasks.getOrNull(0) //mTasks.getOrNull(position)
        if (position == 0) {
            holder.mItem = mValues.get("all_results")
            //holder.mSeeAllResults.text = holder.mView.context.resources.getText(R.string.see_all_results)
            holder.mAllResults.visibility = View.VISIBLE
            holder.mView.setOnClickListener {
                //holder.mItem?.let { mListener?.onTaskDetailFragmentSwipeInteraction(it) }
                holder.mItem?.let { mFragment?.onItemClicked(task, (it as ArrayList<Result>), 0) }
            }
        }
        else if (position == 1 && (task?.isPassFail)!! == false) {
            holder.mItem = mValues.get("high_score")
            holder.mTaskTitleView.text = Utils.formatStringFirstLetterCapital(task?.title)
            holder.mResultTextView.text =
                    if ((holder.mItem as Result).items_correct > 0) {
                        ((holder.mItem as Result).items_correct).toString()
                    } else {
                        ""
                    }

            holder.mResultTextView.visibility = View.VISIBLE
            holder.mHighScoreTextView.visibility = View.VISIBLE
            holder.mTapToViewDetails.visibility = View.VISIBLE
            holder.mLongPressScreenShot.visibility = View.VISIBLE

            holder.mView.setOnClickListener {
                //holder.mItem?.let { mListener?.onTaskDetailFragmentSwipeInteraction(it) }
                holder.mItem?.let { mFragment?.onItemClickedShowHighScoreDetail(task, (it as Result), 1) }
            }
            holder.mView.setOnLongClickListener {
                holder.mItem?.let { mFragment?.onItemLongPressed(task, (it as Parcelable), 1) }
                true
            }
        }

    }

    override fun getItemCount(): Int {
        return if (mValues.containsKey("high_score")) {
            2
        } else {
            1
        } //mValues.size
    }

    fun isHigScore(result: Result) : Boolean {
        var res = false
        val high_score = mValues.get("high_score")
        if (high_score == null) {
            res = true
        }
        else if (result.items_correct > (high_score as Result).items_correct) {
            res = true
        }
      /*  else if (result is ResultTyping && high_score is ResultTyping) {
            if (result.words_correct > high_score.words_correct) {
                res = true
            }
        }
        else if (result is ResultGestures && high_score is ResultGestures) {
            if (result.items_correct > high_score.items_correct) {
                res = true
            }
        }   */

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

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        //SEE ALL RESULTS: Start
        val mAllResults: TextView
        //SEE ALL RESULTS: End

        //HIGH SCORE: Start
        val mTaskTitleView: TextView
        val mResultTextView: TextView
        val mHighScoreTextView: TextView
        val mTapToViewDetails: TextView
        val mLongPressScreenShot: TextView
        //HIGH SCORE: End
        var mItem: Any? = null

        init {
            mAllResults = mView.findViewById<TextView>(R.id.tv_all_results) as TextView

            mTaskTitleView = mView.findViewById<TextView>(R.id.tv_task_title) as TextView
            mResultTextView = mView.findViewById<TextView>(R.id.tv_result) as TextView
            mHighScoreTextView = mView.findViewById<TextView>(R.id.tv_high_score) as TextView
            mTapToViewDetails = mView.findViewById<TextView>(R.id.tv_tap_to_view_details) as TextView
            mLongPressScreenShot = mView.findViewById<TextView>(R.id.tv_long_press_screenshot) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mResultTextView.text + "'"
        }
    }
}