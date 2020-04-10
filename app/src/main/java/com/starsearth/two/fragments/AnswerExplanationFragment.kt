package com.starsearth.two.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.starsearth.two.R
import com.starsearth.two.domain.Response
import kotlinx.android.synthetic.main.fragment_answer_explanation.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_RESPONSE = "response"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AnswerExplanationFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AnswerExplanationFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AnswerExplanationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var response: Response
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            response = it.getParcelable(ARG_RESPONSE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_answer_explanation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvExplanation?.text = response.expectedAnswerExplanation
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
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
        fun onAnswerExplanationFragmentInteraction()
    }

    companion object {
        val TAG = "ANSWER_EXPLANATION_FRAGMENT"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AnswerExplanationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(response: Response) =
                AnswerExplanationFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_RESPONSE, response)
                    }
                }
    }
}
