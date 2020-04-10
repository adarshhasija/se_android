package com.starsearth.two.fragments

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
import com.starsearth.two.R
import com.starsearth.two.adapter.MySearchResultItemRecyclerViewAdapter

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SearchResultItemFragment.OnListFragmentInteractionListener] interface.
 */
class SearchResultItemFragment : Fragment() {


    private var columnCount = 1
    private lateinit var mContext: Context
    private var resultType: String? = null

    private var resultsList : ArrayList<Parcelable> = ArrayList()

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            resultsList.addAll(it.getParcelableArrayList<Parcelable>(ARG_RESULTS))
            resultType = it.getString(ARG_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_searchresultitem_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MySearchResultItemRecyclerViewAdapter(mContext, resultsList, resultType, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
            mContext = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnSearchResultListFragmentInteractionListener")
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

        fun onSearchResultListFragmentInteraction(selectedItem: Parcelable, type: String?)
    }

    companion object {

        val TAG = "SEARCH_RESULTS_FRAG"
        const val ARG_RESULTS = "results"
        const val ARG_TYPE = "type"


        @JvmStatic
        fun newInstance(resultsList: ArrayList<Parcelable>, type: String) =
                SearchResultItemFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(ARG_RESULTS, resultsList)
                        putString(ARG_TYPE, type)
                    }
                }
    }
}
