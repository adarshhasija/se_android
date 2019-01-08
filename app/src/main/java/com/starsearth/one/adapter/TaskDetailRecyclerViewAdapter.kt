package com.starsearth.one.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.SETeachingContent
import com.starsearth.one.domain.Task
import com.starsearth.one.fragments.lists.DetailListFragment

import com.starsearth.one.fragments.lists.DetailListFragment.OnTaskDetailListFragmentListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnTaskDetailListFragmentListener].
 *
 */
class TaskDetailRecyclerViewAdapter(private val context: Context, private val mTeachingContent : SETeachingContent?, private val mListTitles: ArrayList<DetailListFragment.LIST_ITEM>, private val mResults: ArrayList<Result>, private val mListener: OnTaskDetailListFragmentListener?, private val mFragment: DetailListFragment) : RecyclerView.Adapter<TaskDetailRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layoutId = R.layout.task_detail_list_item

        val view = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
                //.inflate(R.layout.fragment_resulttyping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemTitle = mListTitles[position]
        holder.mItem = itemTitle
        when (itemTitle) {
            //Course
            DetailListFragment.LIST_ITEM.SEE_PROGRESS -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.GONE
                holder.mHeading1.text = context?.resources?.getString(R.string.see_progress)
                holder.mHeading2.text = ""
            }
            DetailListFragment.LIST_ITEM.KEYBOARD_TEST -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.VISIBLE
                holder.mHeading1.text = context?.resources?.getString(R.string.keyboard_test)
                holder.mHeading2.text = context?.resources?.getString(R.string.keyboard_test_why)
            }
            DetailListFragment.LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.GONE
                holder.mHeading1.text = context?.resources?.getString(R.string.repeat_tasks_you_passed)
                holder.mHeading2.text = ""
            }
            DetailListFragment.LIST_ITEM.SEE_RESULTS_OF_ATTEMPTED_TASKS -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.GONE
                holder.mHeading1.text = context?.resources?.getString(R.string.see_results_of_tasks_you_attempted)
                holder.mHeading2.text = ""
            }

            //Task
            DetailListFragment.LIST_ITEM.ALL_RESULTS -> {
                holder.mHeading1.visibility = View.VISIBLE
                holder.mHeading2.visibility = View.GONE
                holder.mHeading1.text = context?.resources?.getString(R.string.all_results)
                holder.mHeading2.text = ""
            }
            DetailListFragment.LIST_ITEM.HIGH_SCORE -> {
                holder.mResultTextView.visibility = View.VISIBLE
                holder.mHighScoreTextView.visibility = View.VISIBLE
                holder.mTapToViewDetails.visibility = View.VISIBLE
                holder.mLongPressScreenShot.visibility = View.VISIBLE

                holder.mTaskTitleView.text = Utils.formatStringFirstLetterCapital((mTeachingContent as Task)?.title)
                holder.mResultTextView.text = mTeachingContent?.getHighScoreResult(mResults)?.items_correct?.toString()
                holder.mView.setOnLongClickListener {
                    holder.mItem?.let {
                        mListener?.onDetailListItemLongPress(it, mTeachingContent, mResults)
                    }
                    true
                }
            }
            else -> {
            }
        }
        holder.mView.setOnClickListener {
            holder.mItem?.let { mListener?.onDetailListItemTap(it, mTeachingContent, mResults) }
        }
    }

    override fun getItemCount(): Int {
        return mListTitles.size
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

        var mItem: DetailListFragment.LIST_ITEM? = null

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
