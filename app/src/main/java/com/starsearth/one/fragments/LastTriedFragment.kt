package com.starsearth.one.fragments


import android.os.Bundle
import android.os.Parcelable
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.*
import kotlinx.android.synthetic.main.fragment_last_tried.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "TEACHING_CONTENT"
private const val ARG_PARAM2 = "RESULT"
private const val ARG_PARAM3 = "ERROR_TITLE"
private const val ARG_PARAM4 = "ERROR_MESSAGE"

/**
 * A simple [Fragment] subclass.
 * Use the [LastTriedFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LastTriedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mTeachingContent: Any? = null
    private var mResult: Any? = null
    private var mTitle: String? = null      //Error or Checkpoint
    private var mMessage: String? = null    //Error message or Checkpoint message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mTeachingContent = it.getParcelable(ARG_PARAM1)
            mResult = it.getParcelable(ARG_PARAM2)
            mTitle = it.getString(ARG_PARAM3)
            mMessage = it.getString(ARG_PARAM4)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_last_tried, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
     /*   if (mTitle != null && mMessage != null) {
            setMessageUI(mTitle, mMessage)
        }
        else if (mTeachingContent is Course) {
            setLastTriedUI(mTeachingContent, (mResult as Result))
        }   */
        tvTitle?.visibility = View.VISIBLE
        tvTimestamp?.visibility =
                if (mTeachingContent != null) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvMessage?.visibility =
                if (!mMessage.isNullOrEmpty()) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvResult?.visibility =
                if (mTeachingContent != null) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvLongPressCloseScreen?.visibility = View.VISIBLE

        tvTitle?.text =
                if (!mTitle.isNullOrEmpty()) {
                    mTitle
                }
                else if (mTeachingContent != null && mResult != null) {
                    context?.resources?.getString(R.string.result)
                }
                else {
                    ""
                }
        tvTimestamp?.text =
                if (mResult != null) {
                    Utils.formatDate((mResult as Result).timestamp)
                }
                else {
                    ""
                }
        tvMessage?.text =
                if (!mMessage.isNullOrEmpty()) {
                    mMessage
                }
                else {
                    ""
                }
        tvResult?.text =
                if (mTeachingContent is Course && (mTeachingContent as Course)?.getTaskById((mResult as Result)?.task_id).isPassFail) {
                    (mResult as ResultTyping).getScoreSummary(context, true, (mTeachingContent as Course)?.getTaskById((mResult as Result).task_id).passPercentage)
                }
                else if (mTeachingContent is Task) {
                    ((mResult as Result).items_correct).toString()
                }
                else {
                    ""
                }
        tvLongPressCloseScreen?.text =
                if ((activity?.application as StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                    context?.resources?.getText(R.string.tap_long_press_to_close_this_screen)
                }
                else {
                    context?.resources?.getText(R.string.long_press_to_close_this_screen)
                }

        clMain?.visibility = View.VISIBLE
        clMain?.contentDescription =
                if (!mTitle.isNullOrEmpty() && !mMessage.isNullOrEmpty()) {
                    tvTitle?.text.toString() + " " + tvMessage?.text.toString() + tvLongPressCloseScreen?.text.toString()
                }
                else if (mTeachingContent != null && mResult != null) {
                    tvTitle?.text.toString() + " " + tvResult?.text.toString() + tvLongPressCloseScreen?.text.toString()
                }
                else {
                    ""
                }
        clMain?.announceForAccessibility(
                if (!mTitle.isNullOrEmpty() && !mMessage.isNullOrEmpty()) {
                    tvTitle?.text.toString() + " " + tvMessage?.text.toString() + tvLongPressCloseScreen?.text.toString()
                }
                else if (mTeachingContent != null && mResult != null) {
                    tvTitle?.text.toString() + " " + tvResult?.text.toString() + tvLongPressCloseScreen?.text.toString()
                }
                else {
                    ""
                }
        )
        clMain?.setOnLongClickListener(View.OnLongClickListener {
            activity?.supportFragmentManager?.popBackStackImmediate()!!
        })

    }

    fun onEnterTapped() {
        activity?.supportFragmentManager?.popBackStackImmediate()!!
    }

    private fun setMessageUI(errorTitle: String?, errorMessage: String?) {
        view?.findViewById<TextView>(R.id.tvTitle)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tvTitle)?.text = errorTitle
        view?.findViewById<TextView>(R.id.tvMessage)?.text = errorMessage
        view?.findViewById<TextView>(R.id.tvMessage)?.visibility = View.VISIBLE
    }

    private fun setLastTriedUI(teachingContent: Any?, result: Result) {
        view?.findViewById<TextView>(R.id.tvTitle)?.visibility = View.GONE

        view?.findViewById<TextView>(R.id.tvTitle)?.text =
                if (teachingContent is Course && teachingContent.isCheckpointReached(result)) {
                    context?.resources?.getString(R.string.checkpoint_reached) + "\n" + (teachingContent.checkpoints.get(result.task_id) as Checkpoint).title
                }
                else {
                    ""
                }
        view?.findViewById<TextView>(R.id.tvTimestamp)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tvTimestamp)?.text = Utils.formatDate(result.timestamp)

        view?.findViewById<TextView>(R.id.tvResult)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tvResult)?.text =
                if (teachingContent is Course && teachingContent.getTaskById(result.task_id).isPassFail) {
                    (result as ResultTyping).getScoreSummary(context, true, teachingContent.getTaskById(result.task_id).passPercentage)
                } else {
                    ((result as Result).items_correct).toString()
                }

    }


    companion object {
        val TAG = "LastTriedFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LastTriedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(teachingContent: Parcelable?, result: Parcelable?) =
                LastTriedFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAM1, teachingContent)
                        putParcelable(ARG_PARAM2, result)
                    }
                }

        @JvmStatic
        fun newInstance(errorTitle: String?, errorMessage: String?) =
                LastTriedFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM3, errorTitle)
                        putString(ARG_PARAM4, errorMessage)
                    }
                }
    }
}
