package com.starsearth.one.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.starsearth.one.R
import com.starsearth.one.adapter.TaskDetailRecyclerViewAdapter
import com.starsearth.one.domain.*
import java.util.*
import kotlin.collections.HashSet
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.starsearth.one.application.StarsEarthApplication
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.starsearth.one.activity.FullScreenActivity
import kotlin.collections.ArrayList


/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class TaskDetailListFragment : Fragment() {
    private var mColumnCount = 1
    private var mTeachingContent: Any? = null
    private var mResults = ArrayList<Result>()
    private var mListener: OnTaskDetailListFragmentListener? = null

    override fun onResume() {
        super.onResume()

        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mTeachingContent = arguments!!.getParcelable(ARG_TEACHING_CONTENT)
            val parcelableArrayList = arguments!!.getParcelableArrayList<Parcelable>(ARG_RESULTS)
            for (item in parcelableArrayList) {
                mResults.add((item as Result))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_task_detail_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            if (mColumnCount <= 1) {
                view.layoutManager = LinearLayoutManager(context)
            } else {
                view.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            val tasks = if (mTeachingContent is Course) {
                (mTeachingContent as Course)!!.getTasks()
            } else {
                ArrayList(Arrays.asList(mTeachingContent))
            }
            val results = LinkedHashMap<String, Any>()
            results.put("all_results", mResults)
            //HIGH SCORE: START
            var highScore : Any? = mResults?.get(0)
            for (result in mResults) {
                if (result.items_correct > (highScore as Result)?.items_correct) {
                    highScore = result
                }
            }
            results.put("high_score", highScore!!)
            //HIGH SCORE: END
            view.adapter = TaskDetailRecyclerViewAdapter(tasks as List<Task>, results, mListener, this)
        }

        return view
    }

    fun onItemClicked(task: Task?, results: ArrayList<Result>, position: Int) {
        if (position == 0) {
            sendAnalytics(task!!, "ALL_RESULTS", FirebaseAnalytics.Event.SELECT_CONTENT)
            val fragment = ResultListFragment.newInstance(task, results)
            activity?.getSupportFragmentManager()?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                    ?.replace(R.id.fragment_container_main, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
        }
        if (position == 1) {
            sendAnalytics(task!!, "HIGH_SCORE", FirebaseAnalytics.Event.SELECT_CONTENT)
        }
    }

    fun onItemClickedShowHighScoreDetail(task: Task?, result: Result, position: Int) {
        if (position == 1) {
            sendAnalytics(task!!, "HIGH_SCORE", FirebaseAnalytics.Event.SELECT_CONTENT)
            val fragment = ResultDetailFragment.newInstance(task, result)
            activity?.getSupportFragmentManager()?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                    ?.replace(R.id.fragment_container_main, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
        }
    }

    fun onItemLongPressed(mTask: Task?, mResult: Parcelable, position: Int) {
        if (position == 1) {
            sendAnalytics(mTask!!, "HIGH_SCORE", "LONG_PRESS_CONTENT")
            val intent = Intent(context, FullScreenActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("task", mTask)
            bundle.putParcelable("result", mResult)
            bundle.putString("view_type", "high_score")
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.announceForAccessibility(getString(R.string.more_options_screen_opened))
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnTaskDetailListFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnTaskDetailListFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun sendAnalytics(task: Task, itemCategory: String, action: String) {
        val bundle = Bundle()
        bundle.putInt("CONTENT_ID", task.id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, itemCategory)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, task.type?.toString()?.replace("_", " "))
        bundle.putString("content_name", task.title)
        bundle.putInt("content_timed", if (task.timed) { 1 } else { 0 })
        val application = (activity?.application as StarsEarthApplication)
        application.logActionEvent(action, bundle)
        //mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
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
    interface OnTaskDetailListFragmentListener {
        fun onTaskDetailListFragmentInteraction()
    }

    companion object {

        private val ARG_TEACHING_CONTENT = "teaching_content"
        private val ARG_RESULTS = "results"

        fun newInstance(teachingContent: Parcelable?, results: ArrayList<Result>): TaskDetailListFragment {
            val fragment = TaskDetailListFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, teachingContent)
            args.putParcelableArrayList(ARG_RESULTS, results)
            fragment.arguments = args
            return fragment
        }
    }
}
