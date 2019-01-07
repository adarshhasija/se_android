package com.starsearth.one.fragments

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics

import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.domain.Task
import com.starsearth.one.managers.AnalyticsManager
import java.util.*

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
class ResultDetailFragment : Fragment(), View.OnTouchListener {
    private lateinit var task: Task
    private lateinit var result: Result
    private var listener: OnResultDetailFragmentInteractionListener? = null

    private var x1: Float = 0.toFloat()
    private var x2:Float = 0.toFloat()
    private var y1:Float = 0.toFloat()
    private var y2:Float = 0.toFloat()
    private var actionDownTimestamp : Long = 0
    internal val MIN_DISTANCE = 150
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event?.getX()
                y1 = event?.getY()
                actionDownTimestamp = Calendar.getInstance().timeInMillis
            }
            MotionEvent.ACTION_UP -> {
                val actionUpTimestamp = Calendar.getInstance().timeInMillis
                x2 = event?.getX()
                y2 = event?.getY()
                val deltaX = x2 - x1
                val deltaY = y2 - y1
                if (Math.abs(deltaX) > MIN_DISTANCE || Math.abs(deltaY) > MIN_DISTANCE) {
                    gestureSwipe()
                } else if (Math.abs(actionUpTimestamp - actionDownTimestamp) > 500) {
                    gestureLongPress()
                } else {
                    gestureTap()
                }
            }
        }
        return true
    }

    private fun gestureTap() {

    }

    private fun gestureSwipe() {

    }

    private fun gestureLongPress() {
        if (result.responses != null && result.responses.size > 0) {
            listener?.onResultDetailFragmentInteraction(result)
        }
        else {
            val alertDialog = (activity?.application as StarsEarthApplication)?.createAlertDialog(context)
            alertDialog.setTitle(context?.resources?.getString(R.string.error))
            alertDialog.setMessage(context?.resources?.getString(R.string.responses_not_recorded))
            alertDialog.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            alertDialog.show()
        }
        (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForResultsScreenGesture(task, AnalyticsManager.Companion.GESTURES.LONG_PRESS.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getParcelable(ARG_TASK)
            result = it.getParcelable(ARG_RESULT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tv_date_time).text = Utils.formatDateTime(result.timestamp)

        if (result is ResultTyping) {
            view.findViewById<TextView>(R.id.tv_typing_speed).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_typing_speed).text =
                    context?.resources?.getString(R.string.typing_speed) +
                    ":" +
                    " " +
                    (result as ResultTyping).speedWPM
            view.findViewById<TextView>(R.id.tv_accuracy).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_accuracy).text =
                    context?.resources?.getString(R.string.accuracy) +
                    ":" +
                    " " +
                    (result as ResultTyping).accuracy +
                    "%"
            view.findViewById<TextView>(R.id.tv_target_accuracy).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_target_accuracy).text =
                    context?.resources?.getString(R.string.target_accuracy) +
                    ":" +
                    " " +
                    "90%"
            view.findViewById<TextView>(R.id.tv_pass_fail).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_pass_fail).text =
                    context?.resources?.getString(R.string.result) +
                    ":" +
                    " " +
                    (result as ResultTyping).getScoreSummary(context, task.isPassFail, task.passPercentage)
            view.findViewById<TextView>(R.id.tv_words_correct).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_words_correct).text =
                    context?.resources?.getString(R.string.words_correct) +
                    ":" +
                    " " +
                    (result as ResultTyping).words_correct
            view.findViewById<TextView>(R.id.tv_words_total_attempted).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_words_total_attempted).text =
                    context?.resources?.getString(R.string.attempted) +
                    ":" +
                    " " +
                    (result as ResultTyping).words_total_finished
            view.findViewById<TextView>(R.id.tv_characters_correct).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_characters_correct).text =
                    context?.resources?.getString(R.string.characters_correct) +
                    ":" +
                    " " +
                    (result as ResultTyping).characters_correct
            view.findViewById<TextView>(R.id.tv_characters_total_attempted).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_characters_total_attempted).text =
                    context?.resources?.getString(R.string.attempted) +
                    ":" +
                    " " +
                    (result as ResultTyping).characters_total_attempted
        }
        else if (result is Result) {
            view.findViewById<TextView>(R.id.tv_items_correct).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_items_correct).text =
                                        context?.resources?.getString(R.string.correct) +
                                        ":" +
                                        " " +
                                        result.items_correct
            view.findViewById<TextView>(R.id.tv_items_total_attempted).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_items_total_attempted).text =
                                        context?.resources?.getString(R.string.attempted) +
                                        ":" +
                                        " " +
                                        result.items_attempted
        }




        view.findViewById<RelativeLayout>(R.id.rl_main)?.setOnTouchListener(this)

        ///ACCESSIBILITY
        var contentDescription = context?.resources?.getString(R.string.move_your_finger_to_top_left_to_get_content)

        val isTalkbackOn = (activity?.application as StarsEarthApplication)?.accessibility?.isTalkbackOn
        if (result.responses != null && result.responses.size > 0) {
            view.findViewById<TextView>(R.id.tv_long_press_responses).visibility = View.VISIBLE
            if (isTalkbackOn == true) {
                view.findViewById<TextView>(R.id.tv_long_press_responses).text =
                        context?.resources?.getString(R.string.tap_long_press_to_view_responses)
                contentDescription += " or " + context?.resources?.getString(R.string.tap_long_press_to_view_responses)

            }
        }

        view.findViewById<RelativeLayout>(R.id.rl_main).contentDescription = contentDescription
        //////

    }

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnResultDetailFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnResultDetailFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
        fun onResultDetailFragmentInteraction(result: Any)
    }

    companion object {
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
