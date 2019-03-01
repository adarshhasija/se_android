package com.starsearth.one.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.*
import com.starsearth.one.listeners.SeOnTouchListener
import com.starsearth.one.managers.AnalyticsManager
import kotlinx.android.synthetic.main.fragment_result_detail.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TASK = "task"
private const val ARG_RESULT = "result"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ResultDetailFragment.OnResultDetailFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ResultDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ResultDetailFragment : Fragment(), SeOnTouchListener.OnSeTouchListenerInterface {

    override fun gestureTap() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun gestureSwipe() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun gestureLongPress() {
        mResult?.responses?.let {
            mListener?.onResultDetailFragmentInteraction(mTask.getResponsesForTask(mResult.responses, mResult.startTimeMillis).children as? ArrayList<ResponseTreeNode>,
                    mResult.startTimeMillis,
                    mTask,
                    AnalyticsManager.Companion.GESTURES.LONG_PRESS.toString()
            )
        }

    }

    private lateinit var mTask: Task
    private lateinit var mResult: Result
    private var mListener: OnResultDetailFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mTask = it.getParcelable(ARG_TASK)
            mResult = it.getParcelable(ARG_RESULT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDateTime?.text = Utils.formatDateTime(mResult.timestamp)

        if (mResult is ResultTyping) {
            tv_typing_speed?.visibility = View.VISIBLE
            tv_typing_speed?.text =
                    context?.resources?.getString(R.string.typing_speed) +
                    ":" +
                    " " +
                    (mResult as ResultTyping).speedWPM
            tv_accuracy?.visibility = View.VISIBLE
            tv_accuracy.text =
                    context?.resources?.getString(R.string.accuracy) +
                    ":" +
                    " " +
                    (mResult as ResultTyping).accuracy +
                    "%"
            tv_target_accuracy?.visibility = View.VISIBLE
            tv_target_accuracy?.text =
                    context?.resources?.getString(R.string.target_accuracy) +
                    ":" +
                    " " +
                    "90%"
            tv_pass_fail?.visibility = View.VISIBLE
            tv_pass_fail?.text =
                    context?.resources?.getString(R.string.result) +
                    ":" +
                    " " +
                    (mResult as ResultTyping).getScoreSummary(context, mTask.isPassFail, mTask.passPercentage)
            tv_words_correct?.visibility = View.VISIBLE
            tv_words_correct?.text =
                    context?.resources?.getString(R.string.words_correct) +
                    ":" +
                    " " +
                    (mResult as ResultTyping).words_correct
            tv_words_total_attempted?.visibility = View.VISIBLE
            tv_words_total_attempted?.text =
                    context?.resources?.getString(R.string.attempted) +
                    ":" +
                    " " +
                    (mResult as ResultTyping).words_total_finished
            tv_characters_correct?.visibility = View.VISIBLE
            tv_characters_correct?.text =
                    context?.resources?.getString(R.string.characters_correct) +
                    ":" +
                    " " +
                    (mResult as ResultTyping).characters_correct
            tv_characters_total_attempted?.visibility = View.VISIBLE
            tv_characters_total_attempted?.text =
                    context?.resources?.getString(R.string.attempted) +
                    ":" +
                    " " +
                    (mResult as ResultTyping).characters_total_attempted
        }
        else if (mResult is Result) {
            tv_items_correct?.visibility = View.VISIBLE
            tv_items_correct?.text =
                                        context?.resources?.getString(R.string.correct) +
                                        ":" +
                                        " " +
                                        mResult.items_correct
            tv_items_total_attempted?.visibility = View.VISIBLE
            tv_items_total_attempted?.text =
                                        context?.resources?.getString(R.string.attempted) +
                                        ":" +
                                        " " +
                                        mResult.items_attempted
        }




        rlMain?.setOnTouchListener(SeOnTouchListener(this@ResultDetailFragment))

        ///ACCESSIBILITY
        var contentDescription = context?.resources?.getString(R.string.move_your_finger_to_top_left_to_get_content)

        val isTalkbackOn = (activity?.application as StarsEarthApplication)?.accessibilityManager?.isTalkbackOn
        if (mResult.responses != null && mResult.responses.size > 0) {
            view.findViewById<TextView>(R.id.tv_long_press_responses).visibility = View.VISIBLE
            if (isTalkbackOn == true) {
                view.findViewById<TextView>(R.id.tv_long_press_responses).text =
                        context?.resources?.getString(R.string.tap_long_press_to_view_responses)
                contentDescription += " or " + context?.resources?.getString(R.string.tap_long_press_to_view_responses)

            }
        }

        rlMain?.contentDescription = contentDescription
        //////

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnResultDetailFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnResultDetailFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnResultDetailFragmentInteractionListener {
        fun onResultDetailFragmentInteraction(responses: ArrayList<ResponseTreeNode>?, startTimeMillis: Long, task: Task, action: String)
    }

    companion object {
        val TAG = "RESULT_DETAIL_FRAGMENT"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResultDetailFragment.
         */
        @JvmStatic
        fun newInstance(task: Task, result: Result) =
                ResultDetailFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_TASK, task)
                        putParcelable(ARG_RESULT, result)
                    }
                }
    }
}
