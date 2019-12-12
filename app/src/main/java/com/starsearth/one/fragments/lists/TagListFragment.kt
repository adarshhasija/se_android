package com.starsearth.one.fragments.lists

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.starsearth.one.R
import com.starsearth.one.activity.MainActivity
import com.starsearth.one.adapter.MyTagRecyclerViewAdapter
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.SETeachingContent
import com.starsearth.one.domain.TagListItem
import com.starsearth.one.managers.FirebaseManager
import kotlinx.android.synthetic.main.fragment_autismstory_list.*
import kotlinx.android.synthetic.main.fragment_profile_educator.*
import kotlinx.android.synthetic.main.fragment_tag_list.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [TagListFragment.OnListFragmentInteractionListener] interface.
 */
class TagListFragment : Fragment() {

    private var columnCount = 1
    private lateinit var mTeachingContent: SETeachingContent
    private lateinit var mContext: Context

    private var listener: OnListFragmentInteractionListener? = null

    private val mSelectedTagsListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            llPleaseWait?.visibility = View.GONE
            val key = dataSnapshot?.key
            val map = dataSnapshot?.value
            if (map != null) {
                for (entry in (map as HashMap<*, *>).entries) {
                    val tagName = entry.key as String
                    (view?.list?.adapter as MyTagRecyclerViewAdapter).setSelected(tagName.toLowerCase(Locale.getDefault()).capitalize())
                }

            }
            (view?.list?.adapter as MyTagRecyclerViewAdapter).notifyDataSetChanged()
        }

        override fun onCancelled(p0: DatabaseError?) {
            llPleaseWait?.visibility = View.GONE
        }

    }

    private val mTagsListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            llPleaseWait?.visibility = View.GONE
            val map = dataSnapshot?.value
            if (map != null) {
                for (entry in (map as HashMap<*, *>).entries) {
                    val key = entry.key as String
                    val value = entry.value as Map<String, Any>
                    var newTag = TagListItem(key, value)
                    (list?.adapter as MyTagRecyclerViewAdapter).addItem(newTag)
                }
                (list?.adapter as MyTagRecyclerViewAdapter).notifyDataSetChanged()
                list?.layoutManager?.scrollToPosition(0)

                //Now we look for the ones that were selected
                (activity as? MainActivity)?.mUser?.uid?.let {
                    llPleaseWait?.visibility = View.VISIBLE
                    val firebaseManager = FirebaseManager("teachingcontent")
                    val query = firebaseManager.getQueryForTagsByUserId(mTeachingContent.uid.toString(), it)
                    query.addListenerForSingleValueEvent(mSelectedTagsListener)
                }
            }
            list?.visibility = View.VISIBLE
        }

        override fun onCancelled(p0: DatabaseError?) {
            llPleaseWait?.visibility = View.GONE
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            mTeachingContent = it.getParcelable(ARG_TEACHING_CONTENT)
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
                var dummyArray = ArrayList<TagListItem>()
                adapter = MyTagRecyclerViewAdapter(dummyArray, mTeachingContent, listener)
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
        query.addListenerForSingleValueEvent(mTagsListener)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
            mContext = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTagListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.fragment_tags_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.done -> {
                if (llPleaseWait?.visibility == View.VISIBLE) {
                    return false //In case the user is pressing it multiple times
                }

                val tagListItems = (view?.list?.adapter as MyTagRecyclerViewAdapter).getAllItems()
                val childUpdates = HashMap<String, Any?>()
                for (tagListItem in tagListItems) {
                    val userId = (activity as? MainActivity)?.mUser?.uid
                    if (userId != null) {
                        llPleaseWait?.visibility = View.VISIBLE
                        childUpdates.put("teachingcontent" + "/" + mTeachingContent.uid.toString() + "/tags/" + tagListItem.name.toUpperCase(Locale.getDefault()) + "/" + userId, if (tagListItem.checked) {
                            true
                        } else {
                            null
                        })
                        childUpdates.put("tags" + "/" + tagListItem.name.toUpperCase(Locale.getDefault()) + "/teachingcontent/" + mTeachingContent.uid.toString() + "/" + userId, if (tagListItem.checked) {
                            true
                        } else {
                            null
                        })
                    }

                }

                val mDatabase = FirebaseDatabase.getInstance().reference
                mDatabase.updateChildren(childUpdates)
                        ?.addOnFailureListener {
                            llPleaseWait?.visibility = View.GONE
                            val alertDialog = (activity?.application as? StarsEarthApplication)?.createAlertDialog(mContext)
                            alertDialog?.setTitle(mContext.getString(R.string.error))
                            alertDialog?.setMessage(mContext.getString(R.string.something_went_wrong))
                            alertDialog?.setPositiveButton(getString(android.R.string.ok), null)
                            alertDialog?.show()
                        }
                        ?.addOnSuccessListener {
                            llPleaseWait?.visibility = View.GONE
                            listener?.onTagsSaveCompleted()
                        }

                return true
            }
            else -> {
            }
        }

        return super.onOptionsItemSelected(item)
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
        fun onTagsSaveCompleted()
    }

    companion object {
        val TAG = "TAG_LIST_FRAG"

        // TODO: Customize parameter argument names
        const val ARG_TEACHING_CONTENT = "teaching-content"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(teachingContent: Parcelable) =
                TagListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_TEACHING_CONTENT, teachingContent)
                    }
                }
    }
}
