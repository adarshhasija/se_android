package com.starsearth.two.fragments.lists

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.starsearth.two.R
import com.starsearth.two.adapter.SeOneListItemRecyclerViewAdapter
import com.starsearth.two.domain.SEOneListItem
import androidx.recyclerview.widget.DividerItemDecoration
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
    private lateinit var mContext: Context
    private var mType : SEOneListItem.Type? = null
    private var mListener: OnSeOneListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mType = SEOneListItem.Type.fromString(requireArguments().getString(ARG_TYPE))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_se_one_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            if (mColumnCount <= 1) {
                view.layoutManager =
                    LinearLayoutManager(
                        context,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                view.addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL
                    )
                )
            } else {
                view.layoutManager =
                    GridLayoutManager(
                        context,
                        mColumnCount
                    )
            }
            view.adapter = SeOneListItemRecyclerViewAdapter(context, getData(), mListener)
        }
        return view
    }

    fun getData(): ArrayList<SEOneListItem> {
        val menuItems = ArrayList<SEOneListItem>()
        //menuItems.addAll(SEOneListItem.returnListForType(context, mType))
        menuItems.addAll(SEOneListItem.populateBaseList(context))

        return menuItems
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSeOneListFragmentInteractionListener) {
            mListener = context
            mContext = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnTaskDetailListFragmentListener")
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
