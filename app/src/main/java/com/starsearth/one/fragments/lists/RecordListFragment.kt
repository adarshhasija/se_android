package com.starsearth.one.fragments.lists

//import android.app.Fragment
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.starsearth.one.adapter.MyRecordItemRecyclerViewAdapter
import java.util.*
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
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
    private var mReturnBundle = Bundle()
    private var mTeachingContent : SETeachingContent? = null
    private lateinit var mResults : ArrayList<Result> //Used if screen is for a course
    private var mType : Any? = null
    private var mContent : String? = null
    private var mListener: OnRecordListFragmentInteractionListener? = null
    private var mDatabaseResultsReference: DatabaseReference? = null
    private val mResultsChildListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val result = dataSnapshot.getValue(Result::class.java)

            val adapter = list.adapter
            val itemCount = adapter.itemCount
            for (i in 0 until itemCount) {
                val menuItem = (adapter as MyRecordItemRecyclerViewAdapter).getItem(i)
                if (menuItem.isTaskIdExists(result?.task_id!!)) {
                    if (mTeachingContent != null) {
                        //If it is a course, do not re arrange the order
                        menuItem.results.add(result)
                        adapter.replaceItem(i, menuItem)
                        adapter.notifyItemChanged(i)
                    }
                    else {
                        adapter.removeAt(i) //remove the entry from the list
                        menuItem.results.add(result) //add at the end
                        adapter.addItem(menuItem)
                        adapter.notifyDataSetChanged()
                        list.layoutManager.scrollToPosition(0)
                    }

                }
            }

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val mResultsMultipleValuesListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val adapter = (list.adapter as MyRecordItemRecyclerViewAdapter)
            val map = dataSnapshot?.value
            if (map != null) {
                for (entry in (map as HashMap<*, *>).entries) {
                    val value = entry.value as Map<String, Any>
                    var newResult = Result(value)
                    if (adapter.getTeachingContentType(newResult.task_id) == Task.Type.TYPING) {
                        newResult = ResultTyping(value)
                    }
                    mResults.add(newResult)
                }
                Collections.sort(mResults, ComparatorMainMenuItem())
                insertResults(mResults)
                (list.adapter as? MyRecordItemRecyclerViewAdapter)?.notifyDataSetChanged()
                list?.layoutManager?.scrollToPosition(0)
                mResults.clear() //Clear mResults so that we can accept new results from future fragments
            }

            progressBar?.visibility = View.GONE
            list?.visibility = View.VISIBLE
        }

        override fun onCancelled(p0: DatabaseError?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    /*
    If it is a task as part of a course, set return result for parent
     */
    private fun setReturnResult(results: ArrayList<Parcelable>) {
        val intent = Intent()
        if (mReturnBundle.getParcelableArrayList<Parcelable>("RESULTS") == null) {
            mReturnBundle.putParcelableArrayList("RESULTS", ArrayList())
        }
        mReturnBundle.getParcelableArrayList<Parcelable>("RESULTS")?.addAll(results)
        intent.putExtras(mReturnBundle)
        activity?.setResult(Activity.RESULT_OK, intent)
    }

    fun insertResults(results: List<Result>) {
        for (result in results) {
            insertResult(result)
        }
    }

    fun insertResult(result: Result) {
        val adapter = list.adapter
        val itemCount = adapter.itemCount
        for (i in 0 until itemCount) {
            val menuItem = (adapter as MyRecordItemRecyclerViewAdapter).getItem(i)
            if (menuItem.isTaskIdExists(result.task_id)) {
                if (menuItem.isResultLatest(result)) {
                    menuItem.results.add(result)
                }
                adapter.removeAt(i) //remove the entry from the list
                adapter.addItem(menuItem)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("TAG", "********ON CREATE************")
        mResults = ArrayList()
        if (arguments != null) {
            mType = SEOneListItem.Type.fromString(arguments!!.getString(ARG_TYPE)) //Can come from simple list
                    ?:
                    DetailListFragment.ListItem.valueOf(arguments!!.getString(ARG_TYPE)) //Can come from Courses section REPEAT_PREVIOUSLY_ATTEMPTED_TASKS
            mContent = arguments!!.getString(ARG_CONTENT)
            mTeachingContent = arguments!!.getParcelable(ARG_TEACHING_CONTENT)
            arguments!!.getParcelableArrayList<Result>(ARG_RESULTS)?.let {
                mResults.addAll(it)
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
                mainMenuItems = removeUnattemptedTasks(mainMenuItems, mResults)
                mResults.clear() //Dont need mResults anymore
            }
            view.list.adapter = MyRecordItemRecyclerViewAdapter(getContext(), mainMenuItems, mListener)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //view has to exist by the time this is called
        if (mResults.isEmpty()) {
            //Only call from FirebaseManager if there are no results passed in
            FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
        }

    }

    override fun onResume() {
        super.onResume()

        if (mTeachingContent == null) {
            //This means we are not looking at a course specific list of tasks
            //We are looking at a general list of records and so we should update the list to show last updates
            Log.d("TAG", "************M RESULTS************"+mResults.size)
            insertResults(mResults)
            (list.adapter as? MyRecordItemRecyclerViewAdapter)?.notifyDataSetChanged()
            list?.layoutManager?.scrollToPosition(0)
        }

    }

    /*
    When a task is completed DetailFragment->MainActivity calls this. Insert a result and when the fragment gets focus,
    Update the view
     */
    fun taskCompleted(result: Result) {
        Log.d("TAG", "*******REACHING HERE*********")
        mResults.add(result)
        Log.d("TAG", "*******m RESULTS 1*********"+mResults.size)
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
                    getRecordItemsFromPreviouslyPassedTasks((mTeachingContent as Course).tasks, mResults)
                }
                else if (tag == DetailListFragment.ListItem.SEE_RESULTS_OF_ATTEMPTED_TASKS) {
                    getRecordItemsFromPreviouslyAttemptedTasks((mTeachingContent as Course).tasks, mResults)
                }
                else {
                    //Show everything
                    AssetsFileManager.getAllItems(context)
                }

             /*   if (mTeachingContent is Course && mTypeCourseListItem == DetailListFragment.ListItem.REPEAT_PREVIOUSLY_PASSED_TASKS) {
                    (mTeachingContent as Course).getAllPassedTasks(mResults)
                } else if (mTeachingContent is Course && mTypeCourseListItem == DetailListFragment.ListItem.SEE_RESULTS_OF_ATTEMPTED_TASKS) {
                    (mTeachingContent as Course).getAllAttemptedTasks(mResults)
                } else if (isTimed) {
                    AssetsFileManager.getAllTimedItems(context)
                } else if (isGame) {
                    AssetsFileManager.getAllGames(context)
                } else if (type != null) {
                    AssetsFileManager.getItemsByType(context, type)
                } else if (!tag.isNullOrEmpty()) {
                    AssetsFileManager.getItemsByTag(context, tag)
                } else {
                    //Show everything
                    AssetsFileManager.getAllItems(context)
                }   */

        return mainMenuItems
    }

    private fun setupResultsListener(currentUser: FirebaseUser) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("results")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.orderByChild("userId")?.equalTo(currentUser.uid)
        //query?.addChildEventListener(mResultsChildListener)
        query?.addListenerForSingleValueEvent(mResultsMultipleValuesListener)
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
        // TODO: Update argument type and name
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
