package com.starsearth.one.fragments

import android.content.Context
import android.net.Uri
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
import com.starsearth.one.domain.Result
import com.starsearth.one.domain.ResultTyping
import com.starsearth.one.domain.Task

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "task"
private const val ARG_PARAM2 = "result"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HighScoreFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HighScoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HighScoreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mTask: Task? = null
    private var mResult: Result? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mTask = it.getParcelable(ARG_PARAM1)
            mResult = it.getParcelable(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_high_score, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTaskName = view.findViewById<TextView>(R.id.tvTaskTitle)
        val tvHighScore = view.findViewById<TextView>(R.id.tv_high_score)
        val tvTimeStamp = view.findViewById<TextView>(R.id.tv_timestamp)
        tvTaskName.text = mTask?.title
        val timestamp = mResult?.timestamp
        timestamp?.let { tvTimeStamp.text = Utils.formatDate(it)  }
        tvHighScore.text = (mResult as Result)?.items_correct.toString()
        if (mResult is ResultTyping) {
            tvHighScore.text = (mResult as ResultTyping).getScoreSummary(context, mTask?.timed!!, mTask?.passPercentage!!)
        }

        view.findViewById<ConstraintLayout>(R.id.layout_main).contentDescription =
                getString(R.string.screenshot_view) + " " +
                view.findViewById<TextView>(R.id.tv_timestamp).text.toString() + " " +
                getString(R.string.high_score) + " " +
                view.findViewById<TextView>(R.id.tv_high_score).text.toString()

        view.announceForAccessibility(
                getString(R.string.screenshot_view)
                        + " " + view.findViewById<TextView>(R.id.tv_timestamp).text.toString()
                        + " " + getString(R.string.high_score)
                        + " " + view.findViewById<TextView>(R.id.tv_high_score).text.toString()
        )
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTaskDetailFragmentInteractionListener")
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
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HighScoreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Parcelable?, param2: Parcelable?) =
                HighScoreFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAM1, param1)
                        putParcelable(ARG_PARAM2, param2)
                    }
                }
    }
}
