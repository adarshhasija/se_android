package com.starsearth.one.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
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
class ResultDetailFragment : Fragment() {
    private lateinit var task: Task
    private lateinit var result: Result
    private var listener: OnResultDetailFragmentInteractionListener? = null

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
        if (result is ResultGestures) {
            view.findViewById<TextView>(R.id.tv_items_correct).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_items_correct).text =
                                        context?.resources?.getString(R.string.correct) +
                                        ":" +
                                        " " +
                                        (result as ResultGestures).items_correct
            view.findViewById<TextView>(R.id.tv_items_total_attempted).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_items_total_attempted).text =
                                        context?.resources?.getString(R.string.attempted) +
                                        ":" +
                                        " " +
                                        (result as ResultGestures).items_attempted.toString()
        }
        else if (result is ResultTyping) {
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
                                        (result as ResultTyping).getScoreSummary(context, task.isPassFail)
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

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onResultDetailFragmentInteraction(task, result)
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
        fun onResultDetailFragmentInteraction(task: Task, result: Result)
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
