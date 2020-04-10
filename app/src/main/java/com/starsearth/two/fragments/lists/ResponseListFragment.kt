package com.starsearth.two.fragments.lists

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starsearth.two.R
import com.starsearth.two.adapter.ResponseRecyclerViewAdapter
import com.starsearth.two.domain.ResponseTreeNode

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ResponseListFragment.OnResponseListFragmentInteractionListener] interface.
 */
class ResponseListFragment : Fragment() {

    // TODO: Customize parameters
    private var mResponses : ArrayList<ResponseTreeNode> = ArrayList()
    private var startTimeMillis : Long = 0

    private var listener: OnResponseListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mResponses.addAll(it.getParcelableArrayList(ARG_RESPONSES))
            startTimeMillis = it.getLong(ARG_START_TIME_MILLIS)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_response_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ResponseRecyclerViewAdapter(context, startTimeMillis, mResponses, listener)
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
        fun onResponseListFragmentInteraction(responseTreeNode: ResponseTreeNode)
    }

    companion object {
        val TAG = "ResponseListFragment"

        // TODO: Customize parameter argument names
        const val ARG_RESPONSES    = "responses"
        const val ARG_START_TIME_MILLIS = "start_time_millis"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(responses: ArrayList<ResponseTreeNode>, startTimeMillis: Long) =
                ResponseListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(ARG_RESPONSES, responses)
                        putLong(ARG_START_TIME_MILLIS, startTimeMillis)
                    }
                }
    }
}
