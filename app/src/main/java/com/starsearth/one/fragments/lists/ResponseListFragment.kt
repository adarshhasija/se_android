package com.starsearth.one.fragments.lists

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starsearth.one.R
import com.starsearth.one.adapter.ResponseRecyclerViewAdapter
import com.starsearth.one.domain.Response
import com.starsearth.one.domain.Result

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ResponseListFragment.OnResponseListFragmentInteractionListener] interface.
 */
class ResponseListFragment : Fragment() {

    // TODO: Customize parameters
    private var mHasMoreDetail = false
    private var mResponses : ArrayList<Response> = ArrayList()
    private var startTimeMillis : Long = 0

    private var listener: OnResponseListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mResponses.addAll(it.getParcelableArrayList(ARG_RESPONSES))
            startTimeMillis = it.getLong(ARG_START_TIME_MILLIS)
            mHasMoreDetail = it.getBoolean(ARG_HAS_MORE_DETAIL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_response_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ResponseRecyclerViewAdapter(context, startTimeMillis, mResponses, mHasMoreDetail, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnResponseListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnResponseListFragmentInteractionListener")
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
    interface OnResponseListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onResponseListFragmentInteraction(item: Response?)
    }

    companion object {
        val TAG = "ResponseListFragment"

        // TODO: Customize parameter argument names
        const val ARG_RESPONSES    = "responses"
        const val ARG_START_TIME_MILLIS = "start_time_millis"
        const val ARG_HAS_MORE_DETAIL = "has_more_detail"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(responses: ArrayList<Response>, startTimeMillis: Long, hasMoreDetail: Boolean) =
                ResponseListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(ARG_RESPONSES, responses)
                        putLong(ARG_START_TIME_MILLIS, startTimeMillis)
                        putBoolean(ARG_HAS_MORE_DETAIL, hasMoreDetail)
                    }
                }
    }
}
