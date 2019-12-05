package com.starsearth.one.fragments.lists

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
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
import com.starsearth.one.activity.MainActivity
import com.starsearth.one.adapter.EducatorContentRecyclerViewAdapter
import com.starsearth.one.domain.SETeachingContent
import com.starsearth.one.domain.Task

import com.starsearth.one.fragments.lists.dummy.DummyContent
import com.starsearth.one.fragments.lists.dummy.DummyContent.DummyItem
import com.starsearth.one.managers.FirebaseManager
import kotlinx.android.synthetic.main.fragment_educatorcontent_list.*
import kotlinx.android.synthetic.main.fragment_educatorcontent_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [EducatorContentFragment.OnListFragmentInteractionListener] interface.
 */
class EducatorContentFragment : Fragment() {

    private lateinit var mContext: Context
    private var mUserId : String? = null
    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            //mUserId = it.getString(ARG_USER_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_educatorcontent_list, container, false)


        if (view.list is RecyclerView) {
            with(view) {
                view.list.layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.list.addItemDecoration(DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL))
                view.list.adapter = EducatorContentRecyclerViewAdapter(mContext, ArrayList(), listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseManager = FirebaseManager("teachingcontent")
        val currentUser = (activity as? MainActivity)?.mUser?.uid
        if (currentUser != null) {
            val query = firebaseManager.getQueryForTeachingContentCreatedByUserId(currentUser)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    val adapter = (list?.adapter as? EducatorContentRecyclerViewAdapter)
                    val map = dataSnapshot?.value
                    if (map != null) {
                        for (entry in (map as HashMap<*, *>).entries) {
                            val key = entry.key.toString()
                            val value = entry.value as HashMap<String, Any>
                            if (value.containsKey("dummy") == false) {
                                //The dummy is not an actual task. It was put in the backend to ensure the Firebase returned a HashMap
                                val task = Task(key, value)
                                adapter?.addItem(task)
                            }

                        }
                        adapter?.notifyDataSetChanged()
                    }
                    llPleaseWait?.visibility = View.GONE
                    list?.visibility = View.VISIBLE
                }

                override fun onCancelled(p0: DatabaseError?) {
                    llPleaseWait?.visibility = View.GONE
                }

            })
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
            mContext = context
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
        fun onEducatorContentListFragmentInteraction(teachingContent: Parcelable)
    }

    companion object {
        val TAG = "EDUCATOR_CONTENT_FRAG"
        const val ARG_USER_ID = "user-id"

        @JvmStatic
        fun newInstance() =
                EducatorContentFragment().apply {
                    arguments = Bundle().apply {
                        //putString(ARG_USER_ID, userId)
                    }
                }
    }
}
