package com.starsearth.one.adapter

import android.content.Context
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.domain.Course
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.Task
import com.starsearth.one.fragments.TaskDetailListFragment

import com.starsearth.one.fragments.TaskDetailListFragment.OnTaskDetailListFragmentListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnTaskDetailListFragmentListener].
 *
 */
class TaskDetailRecyclerViewAdapter(private val context: Context, private val mTeachingContent : Any?, private val mListTitles: ArrayList<TaskDetailListFragment.LIST_ITEM>, private val mResults: ArrayList<Result>, private val mListener: OnTaskDetailListFragmentListener?, private val mFragment: TaskDetailListFragment) : RecyclerView.Adapter<TaskDetailRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layoutId = R.layout.task_detail_list_item

        val view = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
                //.inflate(R.layout.fragment_resulttyping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //val teachingContent = mTeachingContent.getOrNull(0)
        val itemTitle = mListTitles[position]
        holder.mItem = itemTitle
        when (itemTitle) {
            //Course
            TaskDetailListFragment.LIST_ITEM.SEE_PROGRESS -> {
                //holder.mSeeAllResults.text = holder.mView.context.resources.getText(R.string.see_all_results)
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.GONE
                holder.mHeading1.text = context?.resources?.getString(R.string.see_progress)
                holder.mHeading2.text = ""
            }
            TaskDetailListFragment.LIST_ITEM.KEYBOARD_TEST -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.VISIBLE
                holder.mHeading1.text = context?.resources?.getString(R.string.keyboard_test)
                holder.mHeading2.text = context?.resources?.getString(R.string.keyboard_test_why)
            }
            TaskDetailListFragment.LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.GONE
                holder.mHeading1.text = context?.resources?.getString(R.string.repeat_tasks_you_passed)
                holder.mHeading2.text = ""
            }
            TaskDetailListFragment.LIST_ITEM.SEE_RESULTS_OF_ATTEMPTED_TASKS -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.GONE
                holder.mHeading1.text = context?.resources?.getString(R.string.see_results_of_tasks_you_attempted)
                holder.mHeading2.text = ""
            }

            //Task
            TaskDetailListFragment.LIST_ITEM.ALL_RESULTS -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.GONE
                holder.mHeading1.text = context?.resources?.getString(R.string.all_results)
                holder.mHeading2.text = ""
            }
            TaskDetailListFragment.LIST_ITEM.HIGH_SCORE -> {
                holder.mResultTextView.visibility = View.VISIBLE
                holder.mHighScoreTextView.visibility = View.VISIBLE
                holder.mTapToViewDetails.visibility = View.VISIBLE
                holder.mLongPressScreenShot.visibility = View.VISIBLE

                holder.mTaskTitleView.text = Utils.formatStringFirstLetterCapital((mTeachingContent as Task)?.title)
                holder.mResultTextView.text = (mTeachingContent as Task)?.getHighScoreResult(mResults)?.items_correct?.toString()
                holder.mView.setOnLongClickListener {
                    holder.mItem?.let { mFragment?.onItemLongPressed(it) }
                    true
                }
            }
            else -> {
            }
        }
        holder.mView.setOnClickListener {
            holder.mItem?.let { mFragment?.onItemClicked(it) }
        }

     /*   if (position == 0 && mTeachingContent is Task) {
            holder.mItem = mListTitles.get("all_results")
            //holder.mSeeAllResults.text = holder.mView.context.resources.getText(R.string.see_all_results)
            holder.mHeading1.visibility = View.VISIBLE
            holder.mHeading2.visibility = View.GONE
            holder.mHeading1.text = context?.resources?.getString(R.string.all_results)
            holder.mHeading2.text = ""
            holder.mView.setOnClickListener {
                //holder.mItem?.let { mListener?.onTaskDetailFragmentSwipeInteraction(it) }
                holder.mItem?.let { mFragment?.onItemClicked(mTeachingContent, (it as ArrayList<Result>), 0) }
            }
        }
        else if (position == 0 && mTeachingContent is Course) {
            holder.mItem = mListTitles.get("all_results")
            holder.mHeading1.visibility = View.VISIBLE
            holder.mHeading2.visibility = View.GONE
            holder.mHeading1.text = context?.resources?.getString(R.string.see_progress)
            holder.mHeading2.text = ""
            holder.mView.setOnClickListener {
                holder.mItem?.let { mFragment?.onItemClicked(mTeachingContent, (it as ArrayList<Result>), 0) }
            }
        }
        else if (position == 1 && mTeachingContent is Course && (mTeachingContent as Course)?.hasKeyboardTest) {
            holder.mHeading1.visibility = View.VISIBLE
            holder.mHeading2.visibility = View.VISIBLE
            holder.mHeading1.text = context?.resources?.getString(R.string.keyboard_test)
            holder.mHeading2.text = context?.resources?.getString(R.string.keyboard_test_why)
            holder.mView.setOnClickListener {
                holder.mItem?.let { mFragment?.onItemClicked(mTeachingContent, (it as ArrayList<Result>), 1) }
            }
        }
        else if (position == 1 && mTeachingContent is Course && mTeachingContent.isFirstTaskPassed((mListTitles.get("all_results") as java.util.ArrayList<Result>))) {
            holder.mItem = mListTitles.get("all_results")
            holder.mHeading1.visibility = View.VISIBLE
            holder.mHeading2.visibility = View.GONE
            holder.mHeading1.text = context?.resources?.getString(R.string.repeat_tasks_you_passed)
            holder.mHeading2.text = ""
            holder.mView.setOnClickListener {
                holder.mItem?.let { mFragment?.onItemClicked(mTeachingContent, (it as ArrayList<Result>), 1) }
            }
        }
        else if (position == 1 && (mTeachingContent is Task && mTeachingContent?.isGame) && mListTitles.containsKey("high_score")) {
            holder.mItem = mListTitles.get("high_score")
            holder.mTaskTitleView.text = Utils.formatStringFirstLetterCapital(mTeachingContent?.title)
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
                holder.mItem?.let { mFragment?.onItemClickedShowHighScoreDetail(mTeachingContent, (it as Result), 1) }
            }
            holder.mView.setOnLongClickListener {
                holder.mItem?.let { mFragment?.onItemLongPressed(mTeachingContent, (it as Parcelable), 1) }
                true
            }
        }   */
    }

    override fun getItemCount(): Int {
        return mListTitles.size/*if (mListTitles.containsKey("high_score")) {
            2
        }
        else if (mTeachingContent is Course && mListTitles.isNotEmpty()) {
            //If it is a Course and there are results
            2
        } else {
            1
        }*/
    }

    fun getHighScore() : Int? {
        var highScore : Result? = mResults?.getOrNull(0)
        if (highScore != null) {
            for (result in mResults) {
                if (result.items_correct > (highScore as Result)?.items_correct) {
                    highScore = result
                }
            }
            //results.put(TaskDetailListFragment.LIST_ITEM.HIGH_SCORE, highScore!!)
        }
        return highScore?.items_correct
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        //SEE ALL RESULTS: Start
        val mHeading1: TextView
        val mHeading2: TextView
        //SEE ALL RESULTS: End

        //SEE PROGRESS: Start
        val mSeeProgress: TextView
        //SEE PROGRESS: End

        //HIGH SCORE: Start
        val mTaskTitleView: TextView
        val mResultTextView: TextView
        val mHighScoreTextView: TextView
        val mTapToViewDetails: TextView
        val mLongPressScreenShot: TextView
        //HIGH SCORE: End

        //REPEAT COMPLETED TASKS: Start
        val mRepeatCompletedTasks: TextView
        //REPEAT COMPLETED TASKS: End

        var mItem: TaskDetailListFragment.LIST_ITEM? = null

        init {
            mHeading1 = mView.findViewById<TextView>(R.id.tvHeading1) as TextView
            mHeading2 = mView.findViewById<TextView>(R.id.tvHeading2) as TextView

            mSeeProgress = mView.findViewById<TextView>(R.id.tv_see_progress) as TextView

            mTaskTitleView = mView.findViewById<TextView>(R.id.tv_task_title) as TextView
            mResultTextView = mView.findViewById<TextView>(R.id.tvResult) as TextView
            mHighScoreTextView = mView.findViewById<TextView>(R.id.tv_high_score) as TextView
            mTapToViewDetails = mView.findViewById<TextView>(R.id.tv_tap_to_view_details) as TextView
            mLongPressScreenShot = mView.findViewById<TextView>(R.id.tv_long_press_screenshot) as TextView

            mRepeatCompletedTasks = mView.findViewById<TextView>(R.id.tv_repeat_completed_tasks) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mResultTextView.text + "'"
        }
    }
}
