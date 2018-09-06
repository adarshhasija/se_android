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
import com.starsearth.one.domain.Course
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.domain.Task

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
    private var mErrorTitle: String? = null
    private var mErrorMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mTeachingContent = it.getParcelable(ARG_PARAM1)
            mResult = it.getParcelable(ARG_PARAM2)
            mErrorTitle = it.getString(ARG_PARAM3)
            mErrorMessage = it.getString(ARG_PARAM4)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_last_tried, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mErrorTitle != null && mErrorMessage != null) {
            setErrorUI(mErrorTitle, mErrorMessage)
        }
        else if (mTeachingContent is Course) {
            setLastTriedUI(mTeachingContent, mResult)
        }
        view?.findViewById<ConstraintLayout>(R.id.layout_main)?.visibility = View.VISIBLE
        view?.findViewById<ConstraintLayout>(R.id.layout_main)?.setOnLongClickListener(View.OnLongClickListener {
            activity?.supportFragmentManager?.popBackStackImmediate()!!
        })

        val isTalkbackOn = (activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn

        if (isTalkbackOn) {
            view.findViewById<TextView>(R.id.tv_long_press_close_screen)?.text = context?.resources?.getText(R.string.tap_long_press_to_close_this_screen)
        }

        if (mErrorTitle != null && mErrorMessage != null) {
            view.findViewById<ConstraintLayout>(R.id.layout_main).contentDescription =
                    view.findViewById<TextView>(R.id.tv_label_top).text.toString() + " " + view.findViewById<TextView>(R.id.tv_error_message).text.toString() + " " + view.findViewById<TextView>(R.id.tv_long_press_close_screen).text.toString()

            view.announceForAccessibility(
                    view.findViewById<TextView>(R.id.tv_label_top).text.toString()
                            + " " + view.findViewById<TextView>(R.id.tv_error_message).text.toString()
                            + " " + view.findViewById<TextView>(R.id.tv_long_press_close_screen).text.toString()
            )
        } else {
            view.findViewById<ConstraintLayout>(R.id.layout_main).contentDescription =
                    view.findViewById<TextView>(R.id.tv_label_top).text.toString() + " " +
                    view.findViewById<TextView>(R.id.tv_result).text.toString() + " " +
                    view.findViewById<TextView>(R.id.tv_long_press_close_screen).text.toString()

            view.announceForAccessibility(
                    view.findViewById<TextView>(R.id.tv_label_top).text.toString()
                            + " " + view.findViewById<TextView>(R.id.tv_result).text.toString()
                            + " " + view.findViewById<TextView>(R.id.tv_long_press_close_screen).text.toString()
            )
        }

    }

    private fun setErrorUI(errorTitle: String?, errorMessage: String?) {
        view?.findViewById<TextView>(R.id.tv_label_top)?.text = errorTitle
        view?.findViewById<TextView>(R.id.tv_error_message)?.text = errorMessage
        view?.findViewById<TextView>(R.id.tv_error_message)?.visibility = View.VISIBLE
    }

    private fun setLastTriedUI(teachingContent: Any?, result: Any?) {
        view?.findViewById<TextView>(R.id.tv_label_top)?.visibility =
                if (teachingContent is Course && (teachingContent as Course).isCheckpointReached((result as Result))) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }

        view?.findViewById<TextView>(R.id.tv_label_top)?.text =
                if (teachingContent is Course && (teachingContent as Course).isCheckpointReached((result as Result))) {
                    context?.resources?.getString(R.string.checkpoint_reached)
                }
                else {
                    ""
                }

        view?.findViewById<TextView>(R.id.tv_result)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tv_result)?.text =
                if (mTeachingContent is Course && (mTeachingContent as Course).getTaskById((result as Result).task_id).isPassFail) {
                    (result as ResultTyping).getScoreSummary(context, true, (mTeachingContent as Course).getTaskById((result as Result).task_id).passPercentage)
                } else {
                    ((result as Result).items_correct).toString()
                }

     /*   if (teachingContent is Task) {
            view?.findViewById<TextView>(R.id.tv_result)?.text =
                    if (result is ResultTyping) {
                        result.getScoreSummary(context, teachingContent.isPassFail, teachingContent.passPercentage)
                    } else {
                        ((result as Result).items_correct).toString()
                    }

            view?.findViewById<TextView>(R.id.tv_result)?.visibility = View.VISIBLE
        }   */

        view?.findViewById<TextView>(R.id.tv_last_tried)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tv_last_tried)?.text = Utils.formatDate(result.timestamp)
    }

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
    }


    companion object {
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
        fun newInstance(teachingContent: Parcelable?, result: Parcelable?, errorTitle: String?, errorMessage: String?) =
                LastTriedFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAM1, teachingContent)
                        putParcelable(ARG_PARAM2, result)
                        putString(ARG_PARAM3, errorTitle)
                        putString(ARG_PARAM4, errorMessage)
                    }
                }
    }
}
