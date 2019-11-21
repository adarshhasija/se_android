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
import com.starsearth.one.adapter.MyProfileEducatorPermissionsRecyclerViewAdapter
import com.starsearth.one.domain.Educator

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ProfileEducatorPermissionsListFragment.OnListFragmentInteractionListener] interface.
 */
class ProfileEducatorPermissionsListFragment : Fragment() {

    private var columnCount = 1
    private lateinit var mContext : Context
    private lateinit var mProfileEducator : Educator

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mProfileEducator = it.getParcelable(ARG_PROFILE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profileeducatorpermissions_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                val listTitles = ArrayList<Educator.PERMISSIONS>()
                if (mProfileEducator.tagging == true) {
                    listTitles.add(Educator.PERMISSIONS.TAGGING)
                }
                adapter = MyProfileEducatorPermissionsRecyclerViewAdapter(mContext.applicationContext, mProfileEducator, listTitles, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            mContext = context
            listener = context
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
        fun onProfileEducatorPermissionsListFragmentInteraction()
    }

    companion object {

        val TAG = "PROFILE_EDUCATOR_PERMISSIONS_FRAG"
        const val ARG_PROFILE = "profile"

        @JvmStatic
        fun newInstance(profileEducator: Parcelable) =
                ProfileEducatorPermissionsListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PROFILE, profileEducator)
                    }
                }
    }
}
