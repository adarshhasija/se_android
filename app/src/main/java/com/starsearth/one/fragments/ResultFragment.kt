package com.starsearth.one.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics

import com.starsearth.one.R
import com.starsearth.one.activity.tasks.TaskTypingActivity
import com.starsearth.one.domain.Task

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ResultFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mTask: Task? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTask = arguments.getParcelable(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_result, container, false)
        v.findViewById(R.id.btn_start).setOnClickListener(View.OnClickListener {
            //onButtonPressed(mTask)
            startTask(mTask!!)
            sendAnalytics(mTask!!)
        })
        val tv = v.findViewById(R.id.tv_instruction)
        val instructions = mTask?.instructions + " " + context.resources.getString(R.string.your_most_recent_score)
        (tv as TextView).text = instructions?.let { String.format(it, mTask?.trials) }


        when (mTask?.type) {
            Task.Type.TYPING_TIMED,
            Task.Type.TYPING_UNTIMED -> {
                val listFragment = ResultTypingFragment.newInstance(mTask!!)
                val transaction = childFragmentManager.beginTransaction()
                transaction.add(R.id.fragment_container_list, listFragment).commit()
            }
            else -> {
            }
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(context, R.string.typing_game_cancelled, Toast.LENGTH_LONG).show()
        }
    }

    private fun sendAnalytics(task: Task) {
        val analyticsBundle = Bundle()
        analyticsBundle.putInt(FirebaseAnalytics.Param.ITEM_ID, task.id)
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.instructions)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button start task")
        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, analyticsBundle)
    }

    private fun startTask(task: Task) {
        val intent = Intent(context, TaskTypingActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable("task", task)
        intent.putExtras(bundle)
        startActivityForResult(intent, 0)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(task: Task?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(task)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
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
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(task: Task?)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "task"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: Task): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putParcelable(ARG_PARAM1, param1)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor