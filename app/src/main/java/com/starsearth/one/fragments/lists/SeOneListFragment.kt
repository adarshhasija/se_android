package com.starsearth.one.fragments.lists

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.starsearth.one.R
import com.starsearth.one.adapter.SeOneListItemRecyclerViewAdapter
import com.starsearth.one.domain.SEOneListItem
import android.support.v7.widget.DividerItemDecoration
import kotlin.collections.ArrayList


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
class SeOneListFragment : Fragment() {
    // TODO: Customize parameters
    private var mColumnCount = 1
    private var mType : SEOneListItem.Type? = null
    private var mList : ArrayList<SEOneListItem> = ArrayList()
    private var mListener: OnSeOneListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mType = SEOneListItem.Type.fromString(arguments!!.getString(ARG_TYPE))
            val array : ArrayList<SEOneListItem>? = arguments!!.getParcelableArrayList(ARG_LIST)
            array?.let { mList.addAll(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_se_one_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            if (mColumnCount <= 1) {
                view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                view.addItemDecoration(DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL))
            } else {
                view.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            view.adapter = SeOneListItemRecyclerViewAdapter(getData(), mListener)
        }
        return view
    }

    fun getData(): ArrayList<SEOneListItem> {
        val menuItems = ArrayList<SEOneListItem>()
        //menuItems.addAll(SEOneListItem.returnListForType(context, mType))
        menuItems.addAll(
                if (mList != null && mList.size > 0) {
                            mList
                         } else {
                            SEOneListItem.populateBaseList(context)
                         }
                )


        return menuItems
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnSeOneListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnTaskDetailListFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
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
    interface OnSeOneListFragmentInteractionListener {
        fun onSeOneListFragmentInteraction(item: SEOneListItem, index: Int)
    }

    companion object {

        // TODO: Customize parameter argument names
        val TAG = "SE_ONE_LIST_FRAGMENT"
        private val ARG_TYPE = "type"
        private val ARG_LIST = "se-one-items-list"

        // TODO: Customize parameter initialization
        fun newInstance(type: SEOneListItem.Type): SeOneListFragment {
            val fragment = SeOneListFragment()
            val args = Bundle()
            args.putString(ARG_TYPE, type.toString())
            fragment.arguments = args
            return fragment
        }

        fun newInstance(list: ArrayList<Parcelable>): SeOneListFragment {
            val fragment = SeOneListFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_LIST, list)
            fragment.arguments = args
            return fragment
        }
    }
}
