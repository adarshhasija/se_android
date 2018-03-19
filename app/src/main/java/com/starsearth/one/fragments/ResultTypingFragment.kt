package com.starsearth.one.fragments

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
import com.google.firebase.database.*

import com.starsearth.one.R
import com.starsearth.one.adapter.MyResultTypingRecyclerViewAdapter
import com.starsearth.one.domain.Task
import com.starsearth.one.domain.ResultTyping

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
class ResultTypingFragment : Fragment() {
    // TODO: Customize parameters
    private var mColumnCount = 1
    private var mTask: Task? = null
    private var mDatabase: DatabaseReference? = null
    private var mListener: OnListFragmentInteractionListener? = null

    private val mChildEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val resultTyping = dataSnapshot.getValue(ResultTyping::class.java)
            if (mTask?.id != resultTyping!!.task_id) {
                return
            }
            val adapter = (view as RecyclerView).adapter
            (adapter as MyResultTypingRecyclerViewAdapter).addItem(0, resultTyping)

            if (adapter.itemCount > 1) {
                //If the list is now more than MAX_NUMBER_IN_LIST items, remove the lowest item
                val lastItem = adapter.getItem(adapter.itemCount - 1)
                mDatabase?.child(lastItem.uid)?.removeValue() //delete from the database
                adapter.removeItem(lastItem)
            }
            adapter.notifyDataSetChanged()
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
            mTask = arguments.getParcelable(ARG_GAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_resulttyping_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            if (mColumnCount <= 1) {
                view.layoutManager = LinearLayoutManager(context)
            } else {
                view.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            val items = ArrayList<ResultTyping>()
            view.adapter = MyResultTypingRecyclerViewAdapter(items, mListener)

            val currentUser = FirebaseAuth.getInstance().currentUser
            mDatabase = FirebaseDatabase.getInstance().getReference("results")
            mDatabase?.keepSynced(true)
            val query = mDatabase?.orderByChild("userId")?.equalTo(currentUser!!.uid)
            query?.addChildEventListener(mChildEventListener);
        }
        return view
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
        mDatabase?.removeEventListener(mChildEventListener)
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
        fun onListFragmentInteraction(item: ResultTyping)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_GAME = "task"

        // TODO: Customize parameter initialization
        fun newInstance(task: Task): ResultTypingFragment {
            val fragment = ResultTypingFragment()
            val args = Bundle()
            args.putParcelable(ARG_GAME, task)
            fragment.arguments = args
            return fragment
        }
    }
}
