package com.starsearth.one.fragments

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
import com.starsearth.one.FileTasks

import com.starsearth.one.R
import com.starsearth.one.adapter.MyMainMenuItemRecyclerViewAdapter
import java.util.*
import android.support.v7.widget.DividerItemDecoration
import com.google.firebase.analytics.FirebaseAnalytics
import com.starsearth.one.activity.KeyboardActivity
import com.starsearth.one.activity.DetailActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.comparator.ComparatorMainMenuItem
import com.starsearth.one.domain.*
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
class MainMenuItemFragment : Fragment() {
    private var mReturnBundle = Bundle()
    private var mTeachingContent : Any? = null
    private var mResults = ArrayList<Result>() //Used if screen is for a course
    private var mTag : String? = null
    private var mTypeCourseListItem : TaskDetailListFragment.LIST_ITEM? = null
    private var mTypeTask : Task.Type? = null
    private var mIsTimed : Boolean = false
    private var mIsGame : Boolean = false
    private var mListener: OnMainMenuFragmentInteractionListener? = null
    private var mDatabaseResultsReference: DatabaseReference? = null
    private val mResultsChildListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val result = dataSnapshot.getValue(Result::class.java)

            val adapter = (view as RecyclerView).adapter
            val itemCount = adapter.itemCount
            for (i in 0 until itemCount) {
                val menuItem = (adapter as MyMainMenuItemRecyclerViewAdapter).getItem(i)
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
                        (view as RecyclerView).layoutManager.scrollToPosition(0)
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
            mTimer?.cancel()
            val adapter = ((view as RecyclerView)?.adapter as MyMainMenuItemRecyclerViewAdapter)
            val map = dataSnapshot?.value
            if (map != null) {
                val results = ArrayList<Result>()
                for (entry in (map as HashMap<*, *>).entries) {
                    val value = entry.value as Map<String, Any>
                    var newResult = Result(value)
                    if (adapter.getTeachingContentType(newResult.task_id) == Task.Type.TYPING) {
                        newResult = ResultTyping(value)
                    }
                    results.add(newResult)
                }
                Collections.sort(results, ComparatorMainMenuItem())
                insertResults(results)
            }

            mListener?.setListFragmentProgressBarVisibility(View.GONE, (view as RecyclerView))
        }

        override fun onCancelled(p0: DatabaseError?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    private val mResultsSingleValueListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            mTimer?.cancel()
            val adapter = ((view as RecyclerView)?.adapter as MyMainMenuItemRecyclerViewAdapter)
            val map = dataSnapshot?.value
            if (map != null) {
                var result = Result((map as Map<String, Any>))
                if (adapter.getTeachingContentType(result.task_id) == Task.Type.TYPING) {
                    result = ResultTyping((map))
                }
                val results : ArrayList<Result> = arrayListOf(result)
                insertResults(results)
            }


            mListener?.setListFragmentProgressBarVisibility(View.GONE, (view as RecyclerView))
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

    fun insertResults(results: ArrayList<Result>) {
        val adapter = (view as RecyclerView).adapter
        for (result in results) {
            insertResult(result)
        }
        adapter.notifyDataSetChanged()
        (view as RecyclerView)?.layoutManager.scrollToPosition(0)
    }

    fun insertResult(result: Result) {
        val adapter = (view as RecyclerView).adapter
        val itemCount = adapter.itemCount
        for (i in 0 until itemCount) {
            val menuItem = (adapter as MyMainMenuItemRecyclerViewAdapter).getItem(i)
            if (menuItem.isTaskIdExists(result?.task_id!!)) {
                if (menuItem.isResultLatest(result)) {
                    menuItem.results.add(result)
                }
                adapter.removeAt(i) //remove the entry from the list
                adapter.addItem(menuItem)
            }
        }
    }

    fun listItemSelected(item: MainMenuItem, position: Int) {
        //mListener?.onResultListFragmentInteraction(item);
        sendAnalytics((item.teachingContent as SEBaseObject))

        val teachingContent = item.teachingContent
        val resultsArray = ArrayList(item.results)
        val intent = Intent(context, DetailActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable("teachingContent", (teachingContent as Parcelable))
        bundle.putParcelableArrayList("results", resultsArray)
        intent.putExtras(bundle)
        startActivityForResult(intent,0)
    }

    fun sendAnalytics(item: SEBaseObject) {
        val bundle = Bundle()
        bundle.putLong(FirebaseAnalytics.Param.ITEM_ID, item.id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item.title)
        if (item is Task) {
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "task")
            bundle.putString("item_interaction_type", item.type?.toString()?.replace("_", " "))
            bundle.putInt("item_timed", if (item.timed) { 1} else { 0 })
        }
        else {
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "course")
        }
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "list_item")
        val application = (activity?.application as StarsEarthApplication)
        application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    internal inner class isLoadingData : TimerTask() {
        override fun run() {
            mListener?.setListFragmentProgressBarVisibility(View.VISIBLE, (view as RecyclerView))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mTeachingContent = arguments!!.getParcelable(ARG_TEACHING_CONTENT)
            val parcelableArrayList = if (arguments!!.containsKey(ARG_RESULTS)) {
                arguments!!.getParcelableArrayList<Parcelable>(ARG_RESULTS)
            } else {
                ArrayList<Parcelable>()
            }
            for (parcelableResult in parcelableArrayList) {
                mResults.add((parcelableResult as Result))
            }
            mTag = arguments!!.getString(ARG_TAG)
            mTypeCourseListItem = if (arguments!!.containsKey(ARG_TYPE_COURSE)) {
                TaskDetailListFragment.LIST_ITEM.fromString(arguments!!.getString(ARG_TYPE_COURSE))
            }
            else {
                null
            }
            mTypeTask = Task.Type.fromInt(arguments!!.getLong(ARG_TYPE_TASK))
            mIsTimed = arguments!!.getBoolean(ARG_TIMED)
            mIsGame = arguments!!.getBoolean(ARG_GAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_mainmenuitem_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            view.layoutManager = LinearLayoutManager(context)
            view.addItemDecoration(DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL))
            val mainMenuItems = getData(mTag, mTypeTask, mIsTimed, mIsGame)
            view.adapter = MyMainMenuItemRecyclerViewAdapter(getContext(), mainMenuItems, mListener, this)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //view has to exist by the time this is called
        if (!mResults.isEmpty()) {
            for (result in mResults) {
                insertResult((result as Result))
            }
        }
        else {
            //Only call from Firebase if there are no results passed in
            FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
        }

    }

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(if (mTeachingContent != null) {
            "CourseItemsList"
        } else {
            this.javaClass.simpleName
        }, activity!!)
        //mFirebaseAnalytics?.setCurrentScreen(activity!!, this.javaClass.simpleName, null /* class override */); //use name to avoid issues with obstrufication

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            val results : ArrayList<Parcelable>? = extras?.getParcelableArrayList("RESULTS");
            if (results != null) {
                setReturnResult(results)
                for (result in results) {
                    insertResult((result as Result))
                }
            }
        }
    }

    fun getData(tag: String?, type: Task.Type?, isTimed: Boolean, isGame: Boolean): ArrayList<MainMenuItem> {
        val mainMenuItems =
                if (mTeachingContent is Course && mTypeCourseListItem == TaskDetailListFragment.LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS) {
                    (mTeachingContent as Course).getAllPassedTasks(mResults)
                } else if (mTeachingContent is Course && mTypeCourseListItem == TaskDetailListFragment.LIST_ITEM.SEE_RESULTS_OF_ATTEMPTED_TASKS) {
                    (mTeachingContent as Course).getAllAttemptedTasks(mResults)
                } else if (isTimed) {
                    FileTasks.getMainMenuItemsTimed(context)
                } else if (isGame) {
                    FileTasks.getMainMenuItemsGames(context)
                } else if (type != null) {
                    FileTasks.getMainMenuItemsByType(context, type)
                } else if (!tag.isNullOrEmpty()) {
                    FileTasks.getMainMenuItemsByTag(context, tag)
                } else {
                    //Show everything
                    FileTasks.getAllMainMenuItems(context)
                }

        return mainMenuItems
    }

    val mTimer : Timer? = null
    private fun setupResultsListener(currentUser: FirebaseUser) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("results")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.orderByChild("userId")?.equalTo(currentUser.uid)
        //query?.addChildEventListener(mResultsChildListener)
        query?.addListenerForSingleValueEvent(mResultsMultipleValuesListener)
        mListener?.setListFragmentProgressBarVisibility(View.VISIBLE, (view as RecyclerView))
        //mTimer = Timer()
        //mTimer.schedule(isLoadingData(), 0, 1000)
    }
    private fun setupResultsListener(currentUser: FirebaseUser, uid: String?) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("results")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.child(uid)
        query?.addListenerForSingleValueEvent(mResultsSingleValueListener)
        //mTimer = Timer()
        //mTimer.schedule(isLoadingData(), 0, 1000)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnMainMenuFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnTaskDetailListFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mTimer?.cancel()
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
    interface OnMainMenuFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onMainMenuListFragmentInteraction(item: MainMenuItem)
        fun setListFragmentProgressBarVisibility(visibility: Int, view: RecyclerView) //This is done for TabbedActivity. Progress Bar must be part of TabbedActivity so it can overlay all tabs
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_TEACHING_CONTENT = "teachingContent"
        private val ARG_RESULTS = "RESULTS"
        private val ARG_TAG = "TAG"
        private val ARG_TYPE_COURSE = "COURSE"
        private val ARG_TYPE_TASK = "TYPE"
        private val ARG_TIMED = "IS_TIMED"
        private const val ARG_GAME = "IS_GAME"

        fun newInstance(): MainMenuItemFragment {
            val fragment = MainMenuItemFragment()
            return fragment
        }

        fun newInstance(course: Parcelable): MainMenuItemFragment {
            val fragment = MainMenuItemFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, course)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(tag: String, type: Long, isTimed: Boolean, isGame: Boolean): MainMenuItemFragment {
            val fragment = MainMenuItemFragment()
            val args = Bundle()
            args.putString(ARG_TAG, tag)
            args.putLong(ARG_TYPE_TASK, type)
            args.putBoolean(ARG_TIMED, isTimed)
            args.putBoolean(ARG_GAME, isGame)
            fragment.arguments = args
            return fragment
        }

        // TODO: Customize parameter initialization
        fun newInstance(course: Parcelable, results: ArrayList<Parcelable>, listItem: TaskDetailListFragment.LIST_ITEM): MainMenuItemFragment {
            val fragment = MainMenuItemFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, course)
            args.putParcelableArrayList(ARG_RESULTS, results)
            args.putString(ARG_TYPE_COURSE, listItem.valueString)
            fragment.arguments = args
            return fragment
        }
    }
}
