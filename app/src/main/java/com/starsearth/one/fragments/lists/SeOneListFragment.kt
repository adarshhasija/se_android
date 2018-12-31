package com.starsearth.one.fragments.lists

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.starsearth.one.R
import com.starsearth.one.adapter.MySeOneListItemRecyclerViewAdapter
import com.starsearth.one.domain.SEOneListItem
import android.support.v7.widget.DividerItemDecoration
import com.starsearth.one.application.StarsEarthApplication
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
    private var mListener: OnSeOneListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mType = SEOneListItem.Type.fromString(arguments!!.getString(ARG_TYPE))
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
            view.adapter = MySeOneListItemRecyclerViewAdapter(getData(), mListener, this)
        }
        return view
    }

    fun getData(): ArrayList<SEOneListItem> {
        val menuItems = ArrayList<SEOneListItem>()
        menuItems.addAll(SEOneListItem.returnListForType(context, mType))
        menuItems.addAll(SEOneListItem.populateBaseList(context))

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

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
        //mFirebaseAnalytics?.setCurrentScreen(activity!!, this.javaClass.simpleName, null /* class override */); //use name to avoid issues with obstrufication
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
        // TODO: Update argument type and name
        fun onSeOneListFragmentInteraction(item: SEOneListItem)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_TYPE = "type"

        // TODO: Customize parameter initialization
        fun newInstance(type: SEOneListItem.Type): SeOneListFragment {
            val fragment = SeOneListFragment()
            val args = Bundle()
            args.putString(ARG_TYPE, type.toString())
            fragment.arguments = args
            return fragment
        }
    }
}
