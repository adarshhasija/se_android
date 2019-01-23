package com.starsearth.one.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.domain.Response
import com.starsearth.one.domain.Task


import com.starsearth.one.fragments.lists.ResponseListFragment.OnResponseListFragmentInteractionListener
import com.starsearth.one.fragments.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_response.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnResponseListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class ResponseRecyclerViewAdapter(
        private val context: Context,
        private val startTime: Long,
        private val mValues: List<Response>,
        private val hasMoreDetail: Boolean,
        private val mListener: OnResponseListFragmentInteractionListener?)
    : RecyclerView.Adapter<ResponseRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Response
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onResponseListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_response, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        var question = context.resources.getString(R.string.question) + ":" + " "
        question += if (item.question.contains("SPELL", true)) {
            context.resources.getString(R.string.spell)
        } else if (item.question.contains("TYPE_CHARACTER", false)) {
            context.resources.getString(R.string.type_character)
        } else {
            item.question
        }
        holder.mQuestion.text = question

        var expectedAnswer = context.resources.getString(R.string.expected) + ":" + " "
        expectedAnswer += if (item.expectedAnswer.contains("SWIPE", true)) {
            context.resources.getString(R.string.swipe_action) + " = " + context.resources.getString(R.string.false_)
        } else if (item.expectedAnswer.contains("TAP", true)) {
            context.resources.getString(R.string.tap_action) + " = " + context.resources.getString(R.string.true_)
        } else if (item.expectedAnswer == " ") {
            context.resources.getString(R.string.space_symbol)
        } else {
            item.expectedAnswer
        }
        holder.mExpectedAnswer.text = expectedAnswer
        if (item.expectedAnswer == " ") holder.mExpectedAnswer.contentDescription =
                context.resources.getString(R.string.expected) + ":" + " " + context.resources.getString(R.string.space) //Need to add the space work for talkback

        var actualAnswer = context.resources.getString(R.string.answer) + ":" + " "
        actualAnswer += if (item.answer.contains("SWIPE", true)) {
            context.resources.getString(R.string.swipe_action) + " = " + context.resources.getString(R.string.false_)
        } else if (item.answer.contains("TAP", true)) {
            context.resources.getString(R.string.tap_action) + " = " + context.resources.getString(R.string.true_)
        } else if (item.answer == " ") {
            context.resources.getString(R.string.space_symbol)
        } else if (item.answer.isNullOrEmpty()) {
            //Means no answer provided
            (context.resources.getString(R.string.no_answer_received)).toUpperCase()
        } else {
            item.answer
        }
        holder.mActualAnswer.text = actualAnswer
        if (item.answer == " ") holder.mActualAnswer.contentDescription =
                context.resources.getString(R.string.answer) + ":" + " " + context.resources.getString(R.string.space) //Need to add the space word for talkback

        holder.mResult.text = if (item.isCorrect) {
            context.resources.getString(R.string.correct)
        } else {
            context.resources.getString(R.string.not_correct)
        }
        holder.mResult.setTextColor(if (item.isCorrect) {
            Color.GREEN
        } else {
            Color.RED
        })

        var timeTakenString = context?.resources?.getString(R.string.time_taken) + ": "
        timeTakenString  += if (position > 0) {
            Utils.getTimeTakenFormatted(context, item.timestamp - mValues[position - 1].timestamp)
        } else {
            Utils.getTimeTakenFormatted(context,item.timestamp - startTime)
        }
        holder.mTimeTaken.text = timeTakenString

        holder.mTapToViewDetails.visibility = if (hasMoreDetail) {
            View.VISIBLE
        }
        else {
            View.GONE
        }

        with(holder.mView) {
            tag = item
            if (hasMoreDetail) {
                setOnClickListener(mOnClickListener)
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mQuestion: TextView = mView.tv_question
        val mExpectedAnswer: TextView = mView.tv_expected_answer
        val mActualAnswer: TextView = mView.tv_actual_answer
        val mResult: TextView = mView.tvResult
        val mTimeTaken: TextView = mView.tv_time_taken
        val mTapToViewDetails: TextView = mView.tv_tap_to_view_details

        override fun toString(): String {
            return super.toString() + " '" + mQuestion.text + "'"
        }
    }
}
