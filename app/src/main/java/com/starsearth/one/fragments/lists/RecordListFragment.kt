package com.starsearth.one.fragments.lists

//import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.starsearth.one.managers.AssetsFileManager

import com.starsearth.one.R
import com.starsearth.one.adapter.RecordItemRecyclerViewAdapter
import java.util.*
import android.support.v7.widget.DividerItemDecoration
import com.starsearth.one.comparator.ComparatorMainMenuItem
import com.starsearth.one.domain.*
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.fragment_records_list.view.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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
class RecordListFragment : Fragment() {
    private var mTeachingContent : SETeachingContent? = null
    private lateinit var mPassedInResults : ArrayList<Result> //Passed in results if screen is for a course
    private var mNewlyCompletedResults = ArrayList<Result>() //For newly created results that are returned back from fragments
    private var mType : Any? = null
    private var mContent : String? = null
    private var mListener: OnRecordListFragmentInteractionListener? = null
    private var mDatabaseResultsReference: DatabaseReference? = null

    private val mResultValuesListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val adapter = (list?.adapter as? RecordItemRecyclerViewAdapter)
            val map = dataSnapshot?.value
            if (map != null) {
                val results = ArrayList<Result>()
                for (entry in (map as HashMap<*, *>).entries) {
                    val value = entry.value as Map<String, Any>
                    var newResult = Result(value)
                    if (adapter?.getTeachingContentType(newResult.task_id) == Task.Type.SEE_AND_TYPE) {
                        newResult = ResultTyping(value)
                    }
                    results.add(newResult)
                }
                Collections.sort(results, ComparatorMainMenuItem())
                insertResults(results)
                (list?.adapter as? RecordItemRecyclerViewAdapter)?.notifyDataSetChanged()
                list?.layoutManager?.scrollToPosition(0)
            }

            progressBar?.visibility = View.GONE
            list?.visibility = View.VISIBLE
        }

        override fun onCancelled(p0: DatabaseError?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    fun insertResults(results: List<Result>) {
        for (result in results) {
            insertResult(result)
        }
    }

    /*
        This function is called when results are pulled from the server and populated
     */
    private fun insertResult(result: Result) {
        val adapter = list?.adapter
        val itemCount = adapter?.itemCount
        if (adapter != null && itemCount != null) {
            for (i in 0 until itemCount) {
                val menuItem = (adapter as RecordItemRecyclerViewAdapter).getItem(i)
                if (menuItem.isTaskIdExists(result.task_id)) {
                    if (menuItem.isResultLatest(result)) {
                        menuItem.results.add(result)
                    }
                    adapter.removeAt(i) //remove the entry from the list
                    adapter.addItem(menuItem)
                }
            }
        }

    }

    /*
        This function is called when results newly created and returned from DetailFragment.
        Here we do not check if its latest result. We simply insert
        We are assuming that all results will come from same course
     */
    fun insertNewlyCompletedResults(results: ArrayList<Result>) {
        if (results.size <= 0) {
            return
        }
        val adapter = list.adapter
        val itemCount = adapter.itemCount
        for (i in 0 until itemCount) {
            val menuItem = (adapter as RecordItemRecyclerViewAdapter).getItem(i)
            //Assuming all results returned in the array are from same course/task
            //Only need to check first item in the array
            if (menuItem.isTaskIdExists(results.get(0).task_id)) {
                menuItem.results.addAll(results)
                adapter.removeAt(i)
                adapter.addItem(menuItem)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPassedInResults = ArrayList()
        if (arguments != null) {
            mType = SEOneListItem.Type.fromString(arguments!!.getString(ARG_TYPE)) //Can come from simple list
                    ?:
                    DetailListFragment.ListItem.valueOf(arguments!!.getString(ARG_TYPE)) //Can come from Courses section REPEAT_PREVIOUSLY_ATTEMPTED_TASKS
            mContent = arguments!!.getString(ARG_CONTENT)
            mTeachingContent = arguments!!.getParcelable(ARG_TEACHING_CONTENT)
            arguments!!.getParcelableArrayList<Result>(ARG_RESULTS)?.let {
                mPassedInResults.addAll(it)
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_records_list, container, false)

        // Set the adapter
        if (view.list is RecyclerView) {
            val context = view.getContext()
            view.list.layoutManager = LinearLayoutManager(context)
            view.list.addItemDecoration(DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL))
            var mainMenuItems = getData(mType)
            if (mType == DetailListFragment.ListItem.REPEAT_PREVIOUSLY_PASSED_TASKS) {
                mainMenuItems = removeUnattemptedTasks(mainMenuItems, mPassedInResults)
            }
            view.list.adapter = RecordItemRecyclerViewAdapter(getContext(), mainMenuItems, mListener)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //view has to exist by the time this is called
        if (mPassedInResults.isEmpty()) {
            //Only call from FirebaseManager if there are no results passed in
            FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
        }

    }

    override fun onResume() {
        super.onResume()

        if (mTeachingContent == null && mNewlyCompletedResults.size > 0) {
            //This means we are not looking at a course specific list of tasks
            //We are looking at a general list of records and so we should update the list to show last updates
            //mPassedInResults contains latest updates of just attempted tasks
            //Update list only if mPassedInResults has values
            insertNewlyCompletedResults(mNewlyCompletedResults)
            mNewlyCompletedResults.clear()
            (list.adapter as? RecordItemRecyclerViewAdapter)?.notifyDataSetChanged()
            list?.layoutManager?.scrollToPosition(0)
        }

    }

    /*
    When a task is completed DetailFragment->MainActivity calls this. Insert a result
     The list is updated when the fragment regains focus(onResume)
     */
    fun taskCompleted(result: Result) {
        mNewlyCompletedResults.add(result)
    }

    private fun getRecordItemsFromPreviouslyPassedTasks(taskList: List<Task>, resultsList: List<Result>) : ArrayList<RecordItem> {
        val recordItemList = ArrayList<RecordItem>()
        for (task in taskList) {
            if (task.isPassed(resultsList)) {
                val recordItem = RecordItem(task)
                recordItem.type = DetailListFragment.ListItem.REPEAT_PREVIOUSLY_PASSED_TASKS
                recordItemList.add(recordItem)
            }
        }
        return recordItemList
    }

    private fun getRecordItemsFromPreviouslyAttemptedTasks(taskList: List<Task>, resultsList: List<Result>) : ArrayList<RecordItem> {
        val recordItemList = ArrayList<RecordItem>()
        for (task in taskList) {
            if (task.isAttempted(resultsList)) {
                val recordItem = RecordItem(task)
                recordItem.type = DetailListFragment.ListItem.SEE_RESULTS_OF_ATTEMPTED_TASKS
                recordItemList.add(recordItem)
            }
        }
        return recordItemList
    }

    /*
        If we are in REPEAT_PREVIOUSLY_ATTEMPTED_TASKS mode, we only want to see tasks that we have attempted. Remove the others
     */
    private fun removeUnattemptedTasks(mainMenuItems: List<RecordItem>, resultList: List<Result>) : ArrayList<RecordItem> {
        val returnList = ArrayList<RecordItem>()
        mainMenuItems.forEach {
            val isPassed = (it.teachingContent as? Task)?.isPassed(resultList)
            if (isPassed == true) {
                returnList.add(it)
            }
        }
        return returnList
    }

    private fun getData(tag: Any?): ArrayList<RecordItem> {
        val mainMenuItems =
                if (tag == SEOneListItem.Type.TAG) {
                    AssetsFileManager.getItemsByTag(context, mContent)
                }
                else if (tag == SEOneListItem.Type.GAME) {
                    AssetsFileManager.getAllGames(context)
                }
                else if (tag == SEOneListItem.Type.TIMED) {
                    AssetsFileManager.getAllTimedItems(context)
                }
                else if (tag == DetailListFragment.ListItem.REPEAT_PREVIOUSLY_PASSED_TASKS) {
                    getRecordItemsFromPreviouslyPassedTasks((mTeachingContent as Course).tasks, mPassedInResults)
                }
                else if (tag == DetailListFragment.ListItem.SEE_RESULTS_OF_ATTEMPTED_TASKS) {
                    getRecordItemsFromPreviouslyAttemptedTasks((mTeachingContent as Course).tasks, mPassedInResults)
                }
                else {
                    //Show everything
                    AssetsFileManager.getAllItems(context)
                }

        return mainMenuItems
    }

    private fun setupResultsListener(currentUser: FirebaseUser) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("results")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.orderByChild("userId")?.equalTo(currentUser.uid)
        //query?.addChildEventListener(mResultsChildListener)
        query?.addListenerForSingleValueEvent(mResultValuesListener)
        progressBar?.visibility = View.VISIBLE
        list?.visibility = View.GONE
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnRecordListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnTaskDetailListFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        //mDatabaseResultsReference?.removeEventListener(mResultsChildListener)
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
    interface OnRecordListFragmentInteractionListener {
        fun onRecordListItemInteraction(recordItem: RecordItem, index: Int)
    }

    companion object {

        val TAG = "RECORD_LIST_FRAGMENT"

        // TODO: Customize parameter argument names
        private val ARG_TEACHING_CONTENT = "teachingContent"
        private val ARG_RESULTS = "RESULTS"
        private val ARG_TYPE = "TYPE"
        private val ARG_CONTENT = "CONTENT"

        fun newInstance(type: SEOneListItem.Type, content: String?): RecordListFragment {
            val fragment = RecordListFragment()
            val args = Bundle()
            args.putString(ARG_TYPE, type.toString())
            args.putString(ARG_CONTENT, content)
            fragment.arguments = args
            return fragment
        }


        fun newInstance(course: Parcelable, results: ArrayList<Parcelable>, listItem: DetailListFragment.ListItem): RecordListFragment {
            val fragment = RecordListFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, course)
            args.putParcelableArrayList(ARG_RESULTS, results)
            args.putString(ARG_TYPE, listItem.valueString)
            fragment.arguments = args
            return fragment
        }
    }
}
