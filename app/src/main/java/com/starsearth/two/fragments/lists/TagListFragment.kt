package com.starsearth.two.fragments.lists

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.*
import android.util.Log
import android.view.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.starsearth.two.R
import com.starsearth.two.activity.MainActivity
import com.starsearth.two.adapter.MyTagRecyclerViewAdapter
import com.starsearth.two.application.StarsEarthApplication
import com.starsearth.two.domain.SETeachingContent
import com.starsearth.two.domain.TagListItem
import com.starsearth.two.managers.FirebaseManager
import kotlinx.android.synthetic.main.fragment_autismstory_list.*
import kotlinx.android.synthetic.main.fragment_autismstory_list.list
import kotlinx.android.synthetic.main.fragment_profile_educator.*
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.fragment_records_list.view.*
import kotlinx.android.synthetic.main.fragment_tag_list.view.*
import kotlinx.android.synthetic.main.fragment_tag_list.view.list
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
    private var mTeachingContent: SETeachingContent? = null
    private var mIsModeMultiSelect = false //true = When we are selecting tags for a particular teaching content
    private lateinit var mAdapter : MyTagRecyclerViewAdapter //We are using this because we do not want the list to be recreated when we return to it from another fragment
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
                    (view?.list?.adapter as MyTagRecyclerViewAdapter).setSelected(tagName.toUpperCase(Locale.getDefault()))
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

                if (mTeachingContent != null) {
                    //Call this only if we have a teaching content
                    //Now we look for the ones that were selected
                    (activity as? MainActivity)?.mUser?.uid?.let {
                        llPleaseWait?.visibility = View.VISIBLE
                        val firebaseManager = FirebaseManager("teachingcontent")
                        val query = firebaseManager.getQueryForTagsByUserId(mTeachingContent!!.uid.toString(), it)
                        query.addListenerForSingleValueEvent(mSelectedTagsListener)
                    }
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
            if (mTeachingContent != null) {
                mIsModeMultiSelect = true
            }
        }
        var dummyArray = ArrayList<TagListItem>()
        mAdapter = MyTagRecyclerViewAdapter(dummyArray, mTeachingContent, listener)
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
                //var dummyArray = ArrayList<TagListItem>()
                adapter = mAdapter //MyTagRecyclerViewAdapter(dummyArray, mTeachingContent, listener)

            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        svMain.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (view.list.adapter as? MyTagRecyclerViewAdapter)?.filter?.filter(newText)
                return false
            }
        })

        if (mAdapter.itemCount < 1) {
            //Adapter was already populated before. We are simply returning back to the fragment. No need to repopulate
            list?.visibility = View.GONE
            llPleaseWait?.visibility = View.VISIBLE
            val firebaseManager = FirebaseManager("tags")
            val query = firebaseManager.queryForTags
            query.addListenerForSingleValueEvent(mTagsListener)
        }

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

        if (mIsModeMultiSelect) inflater?.inflate(R.menu.fragment_tags_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.done -> {
                if (llPleaseWait?.visibility == View.VISIBLE) {
                    return false //In case the user is pressing it multiple times
                }

                val tagListItems = (view?.list?.adapter as MyTagRecyclerViewAdapter).getAllFilteredItems()
                val childUpdates = HashMap<String, Any?>()
                for (tagListItem in tagListItems) {
                    val userId = (activity as? MainActivity)?.mUser?.uid
                    if (userId != null) {
                        llPleaseWait?.visibility = View.VISIBLE
                        childUpdates.put("teachingcontent" + "/" + mTeachingContent?.uid.toString() + "/tags/" + tagListItem.name.toUpperCase(Locale.getDefault()) + "/" + userId, if (tagListItem.checked) {
                            true
                        } else {
                            null
                        })
                        childUpdates.put("tags" + "/" + tagListItem.name.toUpperCase(Locale.getDefault()) + "/teachingcontent/" + mTeachingContent?.uid.toString() + "/" + userId, if (tagListItem.checked) {
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
        fun onTagListItemSelected(tagListItem: TagListItem)
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

        @JvmStatic
        fun newInstance() =
                TagListFragment()
    }
}
