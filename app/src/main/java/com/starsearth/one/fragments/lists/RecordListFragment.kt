package com.starsearth.one.fragments.lists

//import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.starsearth.one.comparator.ComparatorMainMenuItem
import com.starsearth.one.domain.*
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.fragment_records_list.view.*
import java.io.ByteArrayOutputStream
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
    private var mCreator : User? = null
    private var mTag : TagListItem? = null
    private var mExpentedTCs : Int? = null //This is used when fetching TCs based on tag search. We have to fetch TCs 1-by-1 one as the Tag object only returns TC id. We use this variable to keep track of how many TCs are fetched so that we can then call the results listener
    private var mListener: OnRecordListFragmentInteractionListener? = null
    private var mDatabaseResultsReference: DatabaseReference? = null

    /*
        This is called when displaying teaching content belonging to an educator
     */
    private val mTeachingContentListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val map = dataSnapshot?.value
            if (map != null && (map as HashMap<*, *>).entries.size > 0) {
                val tcList = ArrayList<SETeachingContent>()
                for (entry in map.entries) {
                    val key = entry.key as String
                    val value = entry.value as HashMap<String, Any>
                    if (value.containsKey("dummy") == false) {
                        //The dummy is not an actual task. It was put in the backend to ensure the Firebase returned a HashMap
                        var tc = Task(key, value)
                        tcList.add(tc)
                    }
                }
                insertTeachingContentItems(tcList)
                (list?.adapter as? RecordItemRecyclerViewAdapter)?.notifyDataSetChanged()
                list?.layoutManager?.scrollToPosition(0)
                //progressBar?.visibility = View.GONE
                //list?.visibility = View.VISIBLE
                FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
            }
            else {
                progressBar?.visibility = View.GONE
                list?.visibility = View.GONE
                tvEmptyList?.visibility = View.VISIBLE
            }

        }
        override fun onCancelled(p0: DatabaseError?) {
            progressBar?.visibility = View.GONE
            list?.visibility = View.GONE
            tvEmptyList?.visibility = View.VISIBLE
        }

    }


    private val mSingleTCItemListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if (mExpentedTCs != null) mExpentedTCs = mExpentedTCs!! - 1
            val key = dataSnapshot?.key
            val map = dataSnapshot?.value as HashMap<String, Any>
            if (key != null && map != null) {
                val task = Task(key, map)
                (list?.adapter as? RecordItemRecyclerViewAdapter)?.addItem(RecordItem(task))
            }
            if (mExpentedTCs != null && mExpentedTCs!! < 1) {
                mExpentedTCs = null
                (list?.adapter as? RecordItemRecyclerViewAdapter)?.notifyDataSetChanged()
                list?.layoutManager?.scrollToPosition(0)
                FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
            }

        }

        override fun onCancelled(p0: DatabaseError?) {
            if (mExpentedTCs != null) mExpentedTCs = mExpentedTCs!! - 1
            if (mExpentedTCs != null && mExpentedTCs!! < 1) {
                mExpentedTCs = null
                (list?.adapter as? RecordItemRecyclerViewAdapter)?.notifyDataSetChanged()
                list?.layoutManager?.scrollToPosition(0)
                FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
            }
        }

    }

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
            tvEmptyList?.visibility = View.GONE
        }

        override fun onCancelled(p0: DatabaseError?) {
            progressBar?.visibility = View.GONE
            list?.visibility = View.VISIBLE
            tvEmptyList?.visibility = View.GONE
        }

    }

    fun insertTeachingContentItems(tcItems: List<SETeachingContent>) {
        for (tc in tcItems) {
            insertTCItem(tc)
        }
    }

    fun insertResults(results: List<Result>) {
        for (result in results) {
            insertResult(result)
        }
    }

    private fun insertTCItem(tc: SETeachingContent) {
        val adapter = list?.adapter
        if (adapter != null) {
            val recordItem = RecordItem(tc)
            (adapter as RecordItemRecyclerViewAdapter).addItem(recordItem)
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
                if (menuItem.isTaskIdExists(result.task_id.toString())) {
                    if (menuItem.isResultLatest(result)) {
                        if (menuItem.results.size > 0) menuItem.results.clear() //We only want to keep 1 record here
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
        val itemCount = adapter?.itemCount ?: 0
        if (itemCount < 1) {
            return
        }
        for (i in 0 until itemCount) {
            val menuItem = (adapter as RecordItemRecyclerViewAdapter).getItem(i)
            //Assuming all results returned in the array are from same course/task
            //Only need to check first item in the array
            if (menuItem.isTaskIdExists(results.get(0).task_id.toString())) {
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
            mType = if (arguments!!.containsKey(ARG_TYPE) == true) {
                        SEOneListItem.Type.fromString(arguments!!.getString(ARG_TYPE)) //Can come from simple list
                        ?:
                        DetailListFragment.ListItem.valueOf(arguments!!.getString(ARG_TYPE)!!) //Can come from Courses section REPEAT_PREVIOUSLY_ATTEMPTED_TASKS
                    }
                    else {
                        null
                    }
            mContent = arguments!!.getString(ARG_CONTENT)
            mTeachingContent = arguments!!.getParcelable(ARG_TEACHING_CONTENT)
            arguments!!.getParcelableArrayList<Result>(ARG_RESULTS)?.let {
                mPassedInResults.addAll(it)
            }
            val searchType = arguments!!.getString(ARG_SEARCH_TYPE)
            if (searchType == "EDUCATOR") {
                mCreator = arguments!!.getParcelable(ARG_SELECTED_SEARCH_ITEM)
            }
            else if (searchType == "TAG") {
                mTag = arguments!!.getParcelable(ARG_SELECTED_SEARCH_ITEM)
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
            var mainMenuItems = ArrayList<RecordItem>() //getDataFromLocalFile(mType)
        /*    var mainMenuItems = getDataFromLocalFile(SEOneListItem.Type.ALL)
            for (mainMenuItem in mainMenuItems) {
                if ((mainMenuItem.teachingContent as Task).id == 2.toLong()) {
                    val map = (mainMenuItem.teachingContent as Task).toMap()
                    val calendar = Calendar.getInstance()
                //    map.put("created", calendar.timeInMillis)
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    val key = (mainMenuItem.teachingContent as Task).id.toString() //databaseReference.push().getKey(); //We want this to be the id from our local json file so that it remains the same whenever changes are made
                //    databaseReference.child("teachingcontent").child(key).child("instructions").setValue(map.get("instructions").toString())

                //    val storageRef = FirebaseStorage.getInstance().getReference().child("images/tc_741.jpg");
                //    val bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.autism_1);
                //    val baos = ByteArrayOutputStream();
                //    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                //    val data = baos.toByteArray();
                //    storageRef.putBytes(data);
                }
            }   */
            if (mType == DetailListFragment.ListItem.REPEAT_PREVIOUSLY_PASSED_TASKS) {
                mainMenuItems = removeUnattemptedTasks(mainMenuItems, mPassedInResults)
            }
            view.list.adapter = RecordItemRecyclerViewAdapter(getContext(), mainMenuItems, mListener)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCreator?.let {
            setupTeachingContentListener(it)
        }
        mTag?.name?.let {
            setupTCByTagListener(it)
        }
        mType?.let {
            val tagAsString = it.toString().toUpperCase(Locale.getDefault())
            setupTCByTagListener(tagAsString)
        }

        //view has to exist by the time this is called
      /*  if (mPassedInResults.isEmpty()) {
            //Only call from FirebaseManager if there are no results passed in
            FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
        }   */

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

    /*
        This function is used when getting data locally from tasks.json
        If we are coming from search flow, tag will be null and this will return an empty array
     */
    private fun getDataFromLocalFile(tag: Any?): ArrayList<RecordItem> {
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
                else if (tag == SEOneListItem.Type.ALL) {
                    AssetsFileManager.getAllItems(context)
                }
                else if (tag == DetailListFragment.ListItem.REPEAT_PREVIOUSLY_PASSED_TASKS) {
                    //Courses only
                    getRecordItemsFromPreviouslyPassedTasks((mTeachingContent as Course).tasks, mPassedInResults)
                }
                else if (tag == DetailListFragment.ListItem.SEE_RESULTS_OF_ATTEMPTED_TASKS) {
                    //Courses only
                    getRecordItemsFromPreviouslyAttemptedTasks((mTeachingContent as Course).tasks, mPassedInResults)
                }
                else {
                    //Return empty array
                    ArrayList()
                }

        return mainMenuItems
    }

    private fun setupTeachingContentListener(creator: User) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("teachingcontent")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.orderByChild("creator")?.equalTo(creator.uid)
        //query?.addChildEventListener(mResultsChildListener)
        progressBar?.visibility = View.VISIBLE
        list?.visibility = View.GONE
        query?.addListenerForSingleValueEvent(mTeachingContentListener)
    }

    private fun setupTCByTagListener(tagName: String) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("tags")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.orderByKey()?.equalTo(tagName.toUpperCase(Locale.getDefault()))
        //query?.addChildEventListener(mResultsChildListener)
        progressBar?.visibility = View.VISIBLE
        list?.visibility = View.GONE
        query?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                val map = dataSnapshot?.value
                if (map != null) {
                    val map1 =  (map as HashMap<*, *>).get(tagName.toUpperCase(Locale.getDefault()))
                    val tcMap =  (map1 as HashMap<*, *>).get("teachingcontent")
                    if (tcMap != null && tcMap is HashMap<*,*>) {
                        mExpentedTCs = tcMap.entries.size
                        for (tcEntry in tcMap.entries) {
                            val key = tcEntry.key as String
                            val databaseRef = FirebaseDatabase.getInstance().getReference("teachingcontent")
                            databaseRef.child(key).addListenerForSingleValueEvent(mSingleTCItemListener)
                        }
                    }
                    else {
                        progressBar?.visibility = View.GONE
                        list?.visibility = View.GONE
                        tvEmptyList?.visibility = View.VISIBLE
                    }

                }
                else {
                    progressBar?.visibility = View.GONE
                    list?.visibility = View.GONE
                    tvEmptyList?.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(p0: DatabaseError?) {
                progressBar?.visibility = View.GONE
                list?.visibility = View.GONE
                tvEmptyList?.visibility = View.VISIBLE
            }

        })
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
        private val ARG_SELECTED_SEARCH_ITEM = "SELECTED_SEARCH_ITEM"
        private val ARG_SEARCH_TYPE = "SEARCH_TYPE"

        fun newInstance(selectedSearchItem: Parcelable, type: String?) : RecordListFragment {
            val fragment = RecordListFragment()
            val args = Bundle()
            args.putParcelable(ARG_SELECTED_SEARCH_ITEM, selectedSearchItem)
            args.putString(ARG_SEARCH_TYPE, type)
            fragment.arguments = args
            return fragment
        }

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
