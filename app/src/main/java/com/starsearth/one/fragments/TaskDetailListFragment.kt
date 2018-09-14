package com.starsearth.one.fragments

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

import com.starsearth.one.R
import com.starsearth.one.adapter.TaskDetailRecyclerViewAdapter
import com.starsearth.one.domain.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.activity.FullScreenActivity
import com.starsearth.one.activity.KeyboardActivity
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

    enum class LIST_ITEM private constructor(val valueString: String) {
        //Course
        SEE_PROGRESS("SEE_PROGRESS"),
        KEYBOARD_TEST("KEYBOARD_TEST"),
        REPEAT_PREVIOUSLY_PASSED_TASKS("REPEAT_PREVIOUS"),  //Closest match so that we dont have to change even if the overall text changes
        SEE_RESULTS_OF_ATTEMPTED_TASKS("SEE_RESULTS_OF_ATTEMPTED"),

        //Task
        ALL_RESULTS("ALL_RESULTS"),
        HIGH_SCORE("HIGH_SCORE");


        companion object {

            fun fromString(i: String): LIST_ITEM? {
                for (type in LIST_ITEM.values()) {
                    if (type.valueString == i) {
                        return type
                    }
                }
                return null
            }
        }
    };

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
            val listTitles = ArrayList<LIST_ITEM>() //LinkedHashMap<LIST_ITEM, Any>()
            if (mTeachingContent is Course) {
                //results.put(LIST_ITEM.SEE_PROGRESS, true)
                listTitles.add(LIST_ITEM.SEE_PROGRESS)
            }
            if (mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                //results.put(LIST_ITEM.KEYBOARD_TEST, true)
                listTitles.add(LIST_ITEM.KEYBOARD_TEST)
            }
            if (mTeachingContent is Course && (mTeachingContent as Course).isFirstTaskPassed(mResults)) {
                //results.put(LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS, true)
                //results.put(LIST_ITEM.SEE_RESULTS_OF_ATTEMPTED_TASKS, true)
                listTitles.add(LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS)
            }
            if (mTeachingContent is Course && (mTeachingContent as Course).isCourseStarted(mResults)) {
                //listTitles.add(LIST_ITEM.SEE_RESULTS_OF_ATTEMPTED_TASKS)
            }
            if (mTeachingContent is Task && mResults.isNotEmpty()) {
                //results.put(LIST_ITEM.ALL_RESULTS, mResults)
                listTitles.add(LIST_ITEM.ALL_RESULTS)
            }
            if (mTeachingContent is Task && mResults.isNotEmpty() && (mTeachingContent as Task).isGame) {
                listTitles.add(LIST_ITEM.HIGH_SCORE)
            }
        /*    if (mTeachingContent is Task && mResults.isNotEmpty() && (mTeachingContent as Task).isGame) {
                //HIGH SCORE: START
                var highScore : Any? = mResults?.getOrNull(0)
                if (highScore != null) {
                    for (result in mResults) {
                        if (result.items_correct > (highScore as Result)?.items_correct) {
                            highScore = result
                        }
                    }
                    listTitles.put(LIST_ITEM.HIGH_SCORE, highScore!!)
                }
                //HIGH SCORE: END
            }   */

            view.adapter = TaskDetailRecyclerViewAdapter(context.applicationContext, mTeachingContent, listTitles, mResults, mListener, this)
        }

        return view
    }

    fun onItemClicked(itemTitle: LIST_ITEM) {
        sendAnalytics((mTeachingContent as SEBaseObject), itemTitle.valueString, FirebaseAnalytics.Event.SELECT_CONTENT)
        when (itemTitle) {
            //Course
            LIST_ITEM.SEE_PROGRESS -> {
                val fragment = CourseProgressListFragment.newInstance((mTeachingContent as Course), mResults as ArrayList<Parcelable>)
                activity?.getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
            LIST_ITEM.KEYBOARD_TEST -> {
                val intent = Intent(activity, KeyboardActivity::class.java)
                startActivity(intent)
            }
            LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS -> {
                val fragment = MainMenuItemFragment.newInstance((mTeachingContent as Course), mResults as ArrayList<Parcelable>, LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS)
                activity?.getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }

            //Task
            LIST_ITEM.ALL_RESULTS -> {
                val fragment = ResultListFragment.newInstance((mTeachingContent as Task), mResults)
                activity?.getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
            LIST_ITEM.HIGH_SCORE -> {
                val fragment = ResultDetailFragment.newInstance((mTeachingContent as Task), (mTeachingContent as Task).getHighScoreResult(mResults))
                activity?.getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
            else -> {

            }
        }

      /*  if (position == 0 && itemTitle is Task) {
            //sendAnalytics(itemTitle!!, itemTitle.getTitle(), FirebaseAnalytics.Event.SELECT_CONTENT)
            val fragment = ResultListFragment.newInstance((mTeachingContent as Task), mResults)
            activity?.getSupportFragmentManager()?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                    ?.replace(R.id.fragment_container_main, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
        }
        else if (position == 0 && itemTitle is Course) {
            //sendAnalytics(itemTitle!!, "COURSE_PROGRESS", FirebaseAnalytics.Event.SELECT_CONTENT)
            val fragment = CourseProgressListFragment.newInstance(itemTitle, results as ArrayList<Parcelable>)
            activity?.getSupportFragmentManager()?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                    ?.replace(R.id.fragment_container_main, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
        }
        else if (position == 1 && itemTitle is Course) {
            //sendAnalytics(itemTitle!!, "COURSE_REPEAT_ITEMS", FirebaseAnalytics.Event.SELECT_CONTENT)
            val fragment = MainMenuItemFragment.newInstance(itemTitle, results as ArrayList<Parcelable>)
            activity?.getSupportFragmentManager()?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                    ?.replace(R.id.fragment_container_main, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
        }   */
    }

    //Separate function because only a single result has to be passed
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

    fun onItemLongPressed(itemTitle: LIST_ITEM) {
        sendAnalytics((mTeachingContent as SEBaseObject), itemTitle.valueString, "LONG_PRESS_CONTENT")
        when (itemTitle) {
            LIST_ITEM.HIGH_SCORE -> {
                val intent = Intent(context, FullScreenActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable("task", (mTeachingContent as Task))
                bundle.putParcelable("result", (mTeachingContent as Task).getHighScoreResult(mResults))
                bundle.putString("view_type", "high_score")
                intent.putExtras(bundle)
                startActivity(intent)
            }
            else -> {

            }
        }

     /*   if (position == 1) {

            val intent = Intent(context, FullScreenActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("task", (mTeachingContent as Task))
            bundle.putParcelable("result", (mTeachingContent as Task).getHighScoreResult(mResults))
            bundle.putString("view_type", "high_score")
            intent.putExtras(bundle)
            startActivity(intent)
        }   */
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

    private fun sendAnalytics(teachingContent: SEBaseObject, listItemTitle: String, action: String) {
        val bundle = Bundle()
        bundle.putLong("CONTENT_ID", teachingContent.id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, listItemTitle)
        bundle.putString("content_name", teachingContent.title)
        if (teachingContent is Task) {
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, teachingContent.type?.toString()?.replace("_", " "))
            bundle.putInt("content_timed", if (teachingContent.timed) { 1 } else { 0 })
        }
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
