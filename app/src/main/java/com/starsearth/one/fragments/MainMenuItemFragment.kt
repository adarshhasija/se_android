package com.starsearth.one.fragments

//import android.app.Fragment
import android.content.Context
import android.os.Bundle
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
                        menuItem.results.removeAt(0) //remove the first(older) result
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
            view.adapter = MyMainMenuItemRecyclerViewAdapter(mainMenuItems, mListener)
            FirebaseAuth.getInstance().currentUser?.let { setupResultsListener(it) }
        }
        return view
    }

    fun getData(): ArrayList<MainMenuItem> {
        val mainMenuItems = FileTasks.getMainMenuItems(getContext())
      /*  val mainMenuItems = ArrayList<MainMenuItem>()
        for (game in games) {
            val mainMenuItem = MainMenuItem()
            mainMenuItem.task = game
            mainMenuItems.add(mainMenuItem)
        }   */
        return mainMenuItems
    }

    private fun setupResultsListener(currentUser: FirebaseUser) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("results")
        mDatabaseResultsReference?.keepSynced(true)
        val query = mDatabaseResultsReference?.orderByChild("userId")?.equalTo(currentUser.uid)
        query?.addChildEventListener(mResultsChildListener)
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
