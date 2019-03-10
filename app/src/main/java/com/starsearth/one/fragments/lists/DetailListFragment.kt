package com.starsearth.one.fragments.lists

import android.content.Context
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
import com.starsearth.one.adapter.DetailRecyclerViewAdapter
import com.starsearth.one.domain.*
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
class DetailListFragment : Fragment() {
    private var mColumnCount = 1
    private var mTeachingContent: SETeachingContent? = null
    private var mResults = ArrayList<Result>()
    private var mListener: OnTaskDetailListFragmentListener? = null

    enum class ListItem constructor(val valueString: String) {
        //Course
        COURSE_DESCRIPTION("COURSE_DESCRIPTION"),
        SEE_PROGRESS("SEE_PROGRESS"),
        KEYBOARD_TEST("KEYBOARD_TEST"),
        REPEAT_PREVIOUSLY_PASSED_TASKS("REPEAT_PREVIOUSLY_PASSED_TASKS"),  //Closest match so that we dont have to change even if the overall text changes
        SEE_RESULTS_OF_ATTEMPTED_TASKS("SEE_RESULTS_OF_ATTEMPTED_TASKS"),

        //Task
        ALL_RESULTS("ALL_RESULTS"),
        HIGH_SCORE("HIGH_SCORE");

        fun fromString(i: String): ListItem? {
            for (type in ListItem.values()) {
                if (type.valueString == i) {
                    return type
                }
            }
            return null
        }
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
            val listTitles = ArrayList<ListItem>()
            if (mTeachingContent is Course) {
                listTitles.add(ListItem.COURSE_DESCRIPTION)
            }
            if (mTeachingContent is Course) {
                listTitles.add(ListItem.SEE_PROGRESS)
            }
            if (mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                listTitles.add(ListItem.KEYBOARD_TEST)
            }
            if (mTeachingContent is Course && (mTeachingContent as Course).isFirstTaskPassed(mResults)) {
                listTitles.add(ListItem.REPEAT_PREVIOUSLY_PASSED_TASKS)
            }
            if (mTeachingContent is Course && (mTeachingContent as Course).isCourseStarted(mResults)) {
                listTitles.add(ListItem.SEE_RESULTS_OF_ATTEMPTED_TASKS)
            }
            if (mTeachingContent is Task && mResults.isNotEmpty()) {
                listTitles.add(ListItem.ALL_RESULTS)
            }
            if (mTeachingContent is Task && mResults.isNotEmpty() && (mTeachingContent as Task).isGame) {
                listTitles.add(ListItem.HIGH_SCORE)
            }

            view.adapter = DetailRecyclerViewAdapter(context.applicationContext, mTeachingContent, listTitles, mResults, mListener)
        }

        return view
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
        fun onDetailListItemTap(itemTitle: ListItem, teachingContent: SETeachingContent?, results: ArrayList<Result>)
        fun onDetailListItemLongPress(itemTitle: ListItem, teachingContent: SETeachingContent?, results: ArrayList<Result>)
    }

    companion object {
        val TAG = "DETAIL_LIST_FRAGMENT"
        private val ARG_TEACHING_CONTENT = "teaching_content"
        private val ARG_RESULTS = "results"

        fun newInstance(teachingContent: Parcelable?, results: ArrayList<Result>): DetailListFragment {
            val fragment = DetailListFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, teachingContent)
            args.putParcelableArrayList(ARG_RESULTS, results)
            fragment.arguments = args
            return fragment
        }
    }
}
