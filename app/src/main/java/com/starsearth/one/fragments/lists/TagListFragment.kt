package com.starsearth.one.fragments.lists

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.starsearth.one.R
import com.starsearth.one.adapter.MyTagRecyclerViewAdapter
import com.starsearth.one.domain.Tag

import com.starsearth.one.managers.FirebaseManager
import kotlinx.android.synthetic.main.fragment_autismstory_list.*
import kotlinx.android.synthetic.main.fragment_profile_educator.*
import kotlinx.android.synthetic.main.fragment_tag_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [TagListFragment.OnListFragmentInteractionListener] interface.
 */
class TagListFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null

    private val mValuesListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            llPleaseWait?.visibility = View.GONE
            val map = dataSnapshot?.value
            if (map != null) {
                for (entry in (map as HashMap<*, *>).entries) {
                    val key = entry.key as String
                    val value = entry.value as Map<String, Any>
                    var newTag = Tag(key, value)
                    (list?.adapter as MyTagRecyclerViewAdapter).addItem(newTag)
                }
                (list?.adapter as MyTagRecyclerViewAdapter).notifyDataSetChanged()
                list?.layoutManager?.scrollToPosition(0)
            }
            list?.visibility = View.VISIBLE
        }

        override fun onCancelled(p0: DatabaseError?) {
            llPleaseWait?.visibility = View.GONE
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tag_list, container, false)

        // Set the adapter
        if (view.list is RecyclerView) {
            with(view.list) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.list.addItemDecoration(DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL))
                var dummyArray = ArrayList<Tag>()
                adapter = MyTagRecyclerViewAdapter(dummyArray, listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list?.visibility = View.GONE
        llPleaseWait?.visibility = View.VISIBLE

        val firebaseManager = FirebaseManager("tags")
        val query = firebaseManager.queryForTags
        query.addListenerForSingleValueEvent(mValuesListener)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        fun onTagListItemSelected(item: Tag)
    }

    companion object {
        val TAG = "TAG_LIST_FRAG"

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                TagListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
