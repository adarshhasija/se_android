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
import com.starsearth.one.activity.ResultActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.comparator.ComparatorMainMenuItem
import com.starsearth.one.domain.*
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
    // TODO: Customize parameters
    private var mCourse : Any? = null
    private var mListener: OnListFragmentInteractionListener? = null
    private var mDatabaseResultsReference: DatabaseReference? = null
    private val mResultsChildListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val result = dataSnapshot.getValue(Result::class.java)

            val adapter = (view as RecyclerView).adapter
            val itemCount = adapter.itemCount
            for (i in 0 until itemCount) {
                val menuItem = (adapter as MyMainMenuItemRecyclerViewAdapter).getItem(i)
                if (menuItem.isTaskIdExists(result?.task_id!!)) {
                    if (mCourse != null) {
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
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            mTimer?.cancel()
            val map = dataSnapshot?.value
            if (map != null) {
                val results = ArrayList<Result>()
                for (entry in (map as HashMap<*, *>).entries) {
                    val value = entry.value as Map<String, Any>
                    val newResult = Result(value)
                    results.add(newResult)
                }
                Collections.sort(results, ComparatorMainMenuItem())
                for (result in results) {
                    insertResult(result)
                }
            }

            mListener?.setListFragmentProgressBarVisibility(View.GONE, (view as RecyclerView))
        }

        override fun onCancelled(p0: DatabaseError?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    private val mResultsSingleValueListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            mTimer?.cancel()
            val map = dataSnapshot?.value
            if (map == null) { return; }
            val result = Result((map as Map<String, Any>))
            if (result!!.isJustCompleted && mCourse != null) {
                //If it was just completed as this fragment is part of a course,
                //Set return result for parent Main Menu
                setReturnResult(result)
            }
            insertResult(result)

            mListener?.setListFragmentProgressBarVisibility(View.GONE, (view as RecyclerView))
        }

        override fun onCancelled(p0: DatabaseError?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    /*
    If it is a task as part of a course, set return result for parent
     */
    private fun setReturnResult(result: Any?) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putString("uid", (result as Result)?.uid)
        intent.putExtras(bundle)
        activity?.setResult(Activity.RESULT_OK, intent)
    }

    fun insertResult(result: Result) {
        val adapter = (view as RecyclerView).adapter
        val itemCount = adapter.itemCount
        for (i in 0 until itemCount) {
            val menuItem = (adapter as MyMainMenuItemRecyclerViewAdapter).getItem(i)
            if (menuItem.isTaskIdExists(result?.task_id!!)) {
                if (mCourse != null) {
                    //If it is a course, do not re arrange the order
                    menuItem.results.add(result)
                    adapter.replaceItem(i, menuItem)
                    adapter.notifyItemChanged(i)
                }
                else {
                    adapter.removeAt(i) //remove the entry from the list
                    menuItem.results.push(result)
                    adapter.addItem(menuItem)
                    adapter.notifyDataSetChanged()
                    (view as RecyclerView).layoutManager.scrollToPosition(0)
                }
            }
        }
    }

    fun listItemSelected(item: MainMenuItem, position: Int) {
        //mListener?.onListFragmentInteraction(item);
        sendAnalytics((item.teachingContent as SEBaseObject))

        val teachingContent = item.teachingContent
        if (teachingContent is Task && teachingContent.type == Task.Type.KEYBOARD_TEST) {
            val intent = Intent(context, KeyboardActivity::class.java)
            startActivity(intent)
        }
        else {
            val intent = Intent(context, ResultActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("teachingContent", (teachingContent as Parcelable))
            intent.putExtras(bundle)
            startActivityForResult(intent,0)
        }

    }

    fun sendAnalytics(item: SEBaseObject) {
        val bundle = (activity?.application as StarsEarthApplication).userPropertiesAccessibility
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, item.id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item.title)
        if (item is Task) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, item.type?.toString()?.replace("_", " "))
        }
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "list_item")
        val application = (activity?.application as StarsEarthApplication)
        application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }


    /**
     * If course list has been expanded, items below course row will be course tasks
     * Check if next row item is task of this course
     */
    fun isCourseTasksVisible(course: Course, position: Int): Boolean {
        val adapter = (view as RecyclerView).adapter

        //get next row item
        val nextMainMenuItem = (adapter as MyMainMenuItemRecyclerViewAdapter).getItem(position + 1)
        val task = nextMainMenuItem.teachingContent
        var result = false

        //does course contain task of next row item
        if (course.isTaskExists((task as SEBaseObject).id)) {
                    result = true
                }

            return result
    }

    internal inner class isLoadingData : TimerTask() {
        override fun run() {
            mListener?.setListFragmentProgressBarVisibility(View.VISIBLE, (view as RecyclerView))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mCourse = arguments!!.getParcelable(ARG_COURSE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_mainmenuitem_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
           /* if (mCourse <= 1) {
                view.layoutManager = LinearLayoutManager(context)
                view.addItemDecoration(DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL))
            } else {
                view.layoutManager = GridLayoutManager(context, mCourse)
            }   */
            view.layoutManager = LinearLayoutManager(context)
            view.addItemDecoration(DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL))
            val mainMenuItems = getData()
            view.adapter = MyMainMenuItemRecyclerViewAdapter(mainMenuItems, mListener, this)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //view has to exist by the time this is called
        FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
    }

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(if (mCourse != null) {
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
            val uid = extras?.getString("uid")
            FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it, uid) }
        }
    }

    fun getData(): ArrayList<MainMenuItem> {
        val mainMenuItems = if (mCourse != null) {
            FileTasks.getMainMenuItemsFromCourse((mCourse as Course))
        } else {
            FileTasks.getMainMenuItems(getContext())
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
        if (context is OnListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
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
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: MainMenuItem)
        fun setListFragmentProgressBarVisibility(visibility: Int, view: RecyclerView)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_COURSE = "course"

        fun newInstance(): MainMenuItemFragment {
            val fragment = MainMenuItemFragment()
            return fragment
        }

        // TODO: Customize parameter initialization
        fun newInstance(course: Parcelable): MainMenuItemFragment {
            val fragment = MainMenuItemFragment()
            val args = Bundle()
            args.putParcelable(ARG_COURSE, course)
            fragment.arguments = args
            return fragment
        }
    }
}
