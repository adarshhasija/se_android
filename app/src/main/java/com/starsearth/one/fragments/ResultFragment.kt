package com.starsearth.one.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics

import com.starsearth.one.R
import com.starsearth.one.activity.tasks.TaskActivity
import com.starsearth.one.application.StarsEarthApplication
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
    private var mTeachingContent: Any? = null

    private var mListener: OnFragmentInteractionListener? = null

    fun firebaseAnalyticsTaskCompleted(bundle: Bundle) {
        val application = (activity?.application as StarsEarthApplication)
        val score = bundle?.getInt(FirebaseAnalytics.Param.SCORE)
        application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle, score)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTeachingContent = arguments?.getParcelable(ARG_TEACHING_CONTENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_result, container, false)
        v.findViewById<Button>(R.id.btn_start).setOnClickListener(View.OnClickListener {
            //onButtonPressed(mTeachingContent)

            startTaskTyping((mTeachingContent as Task))
            sendAnalytics((mTeachingContent as Task))
        })
        val tv = v.findViewById<TextView>(R.id.tv_instruction)

        (tv as TextView).text =
                (if (mTeachingContent is Task && (mTeachingContent as Task)?.durationMillis > 0) {
                    String.format((mTeachingContent as Task)?.instructions + " " +
                            context?.resources?.getString(R.string.complete_as_many_as) + " " +
                            context?.resources?.getString(R.string.your_best_score), (mTeachingContent as Task)?.getTimeLimitAsString(context))
                } else if (mTeachingContent is Task) {
                    String.format((mTeachingContent as Task)?.instructions + " " +
                            context?.resources?.getString(R.string.do_this_number_times) + " " +
                            context?.resources?.getString(R.string.your_best_score) + " " +
                            context?.resources?.getString(R.string.target_accuracy), (mTeachingContent as Task)?.trials)
                } else {
                    ""
                }).toString()

        val listFragment = ResultListFragment.newInstance((mTeachingContent as Parcelable))
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container_list, listFragment).commit()


        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(context, R.string.typing_game_cancelled, Toast.LENGTH_LONG).show()
        }
    }

    private fun sendAnalytics(task: Task) {
        val analyticsBundle = (activity?.application as StarsEarthApplication).userPropertiesAccessibility
        analyticsBundle.putInt(FirebaseAnalytics.Param.ITEM_ID, task.id)
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.instructions)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button start task")
        val application = (activity?.application as StarsEarthApplication)
        application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, analyticsBundle)
        //mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, analyticsBundle)
    }

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
        //mFirebaseAnalytics?.setCurrentScreen(activity!!, this.javaClass.name, null /* class override */);
    }

    private fun startTaskTyping(task: Task) {
        val intent = Intent(context, TaskActivity::class.java)
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

    fun flashHighScore() {
        val mContentView = view?.findViewById<TextView>(R.id.tv_high_score) as TextView
        mContentView.alpha = 0f
        mContentView.visibility = View.VISIBLE

        mContentView.animate()
                .alpha(1f)
                .setDuration(150)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mContentView.visibility = View.GONE
                    }
                })
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
        private val ARG_COURSE = "course"
        private val ARG_TEACHING_CONTENT = "task"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param0: Parcelable?): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, param0)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
