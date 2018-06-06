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
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.ResultGestures
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.domain.Task

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "TEACHING_CONTENT"
private const val ARG_PARAM2 = "RESULT"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mTeachingContent = it.getParcelable(ARG_PARAM1)
            mResult = it.getParcelable(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_last_tried, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLastTriedUI(mTeachingContent, mResult)
    }

    private fun setLastTriedUI(teachingContent: Any?, result: Any?) {
        if (teachingContent is Task) {
            view?.findViewById<TextView>(R.id.tv_result)?.text =
                    if (result is ResultTyping) {
                        result.getScoreSummary(context, teachingContent.timed)
                    } else if (result is ResultGestures) {
                        result.getScoreSummary(context, teachingContent.type)
                    } else {
                        ""
                    }
        }

        view?.findViewById<TextView>(R.id.tv_last_tried)?.text = Utils.formatDateTime((result as Result).timestamp)
        view?.findViewById<ConstraintLayout>(R.id.layout_last_tried)?.visibility = View.VISIBLE
        view?.findViewById<ConstraintLayout>(R.id.layout_last_tried)?.setOnClickListener(View.OnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        })
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
        fun newInstance(teachingContent: Parcelable?, result: Parcelable?) =
                LastTriedFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAM1, teachingContent)
                        putParcelable(ARG_PARAM2, result)
                    }
                }
    }
}
