package com.starsearth.one.fragments

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
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.Response
import com.starsearth.one.domain.Result

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ResponseListFragment.OnResponseListFragmentInteractionListener] interface.
 */
class ResponseListFragment : Fragment() {

    // TODO: Customize parameters
    private var mResult : Any? = null

    private var listener: OnResponseListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mResult = it.getParcelable(ARG_RESPONSES)
        }
    }

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_response_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ResponseRecyclerViewAdapter(context, (mResult as Result).startTime, (mResult as Result).responses, listener)
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

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_RESPONSES    = "responses"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(result: Any) =
                ResponseListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_RESPONSES, (result as Parcelable))
                    }
                }
    }
}
