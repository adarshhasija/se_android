package com.starsearth.one.fragments

//import android.app.Fragment
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.starsearth.one.FileTasks

import com.starsearth.one.R
import com.starsearth.one.adapter.MyMainMenuItemRecyclerViewAdapter
import com.starsearth.one.domain.MainMenuItem
import com.starsearth.one.domain.Result
import java.util.*
import android.support.v7.widget.DividerItemDecoration
import com.starsearth.one.activity.ResultActivity
import com.starsearth.one.comparator.ComparatorMainMenuItem
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
    private var mColumnCount = 1
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
                    adapter.removeAt(i) //remove the entry from the list

                    menuItem.results.add(result) //add at the end
                    if (menuItem.results.size > 1) {
                        menuItem.results.remove(); //remove the first(older) result
                    }
                    adapter.addItem(menuItem)
                    adapter.notifyDataSetChanged()
                    (view as RecyclerView).layoutManager.scrollToPosition(0)
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
            if (map == null) { return; }
            val results = ArrayList<Result>()
            for (entry in (map as HashMap<*, *>).entries) {
                val newResult = Result((entry.value as Map<String, Any>))
                results.add(newResult)
            }
            Collections.sort(results, ComparatorMainMenuItem())
            for (result in results) {
                insertResult(result)
            }
            mListener?.setListFragmentProgressBarVisibility(View.GONE)
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
            insertResult(result)
            mListener?.setListFragmentProgressBarVisibility(View.GONE)
        }

        override fun onCancelled(p0: DatabaseError?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    fun insertResult(result: Result) {
        val adapter = (view as RecyclerView).adapter
        val itemCount = adapter.itemCount
        for (i in 0 until itemCount) {
            val menuItem = (adapter as MyMainMenuItemRecyclerViewAdapter).getItem(i)
            if (menuItem.isTaskIdExists(result?.task_id!!)) {
                adapter.removeAt(i) //remove the entry from the list

                menuItem.results.add(result) //add at the end
                if (menuItem.results.size > 1) {
                    menuItem.results.remove(); //remove the first(older) result
                }
                adapter.addItem(menuItem)
                adapter.notifyDataSetChanged()
                (view as RecyclerView).layoutManager.scrollToPosition(0)
            }
        }
    }

    fun listItemSelected(item: MainMenuItem) {
        mListener?.onListFragmentInteraction(item);
        val teachingContent = item.teachingContent
        val intent = Intent(context, ResultActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable("teachingContent", (teachingContent as Parcelable))
        intent.putExtras(bundle)
        startActivityForResult(intent,0)
    }

    internal inner class isLoadingData : TimerTask() {
        override fun run() {
            mListener?.setListFragmentProgressBarVisibility(View.VISIBLE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mColumnCount = arguments.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_mainmenuitem_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            if (mColumnCount <= 1) {
                view.layoutManager = LinearLayoutManager(context)
                view.addItemDecoration(DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL))
            } else {
                view.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            val mainMenuItems = getData()
            view.adapter = MyMainMenuItemRecyclerViewAdapter(mainMenuItems, mListener, this)
            FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
        }
        return view
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
        val mainMenuItems = FileTasks.getMainMenuItems(getContext())

        return mainMenuItems
    }

    val mTimer : Timer? = null
    private fun setupResultsListener(currentUser: FirebaseUser) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("results")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.orderByChild("userId")?.equalTo(currentUser.uid)
        //query?.addChildEventListener(mResultsChildListener)
        query?.addListenerForSingleValueEvent(mResultsMultipleValuesListener)
        mListener?.setListFragmentProgressBarVisibility(View.VISIBLE)
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
        mDatabaseResultsReference?.removeEventListener(mResultsChildListener)
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
        fun setListFragmentProgressBarVisibility(visibility: Int)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        fun newInstance(columnCount: Int): MainMenuItemFragment {
            val fragment = MainMenuItemFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}
