package com.starsearth.one.fragments

import android.content.Context
import android.content.Intent
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
import com.starsearth.one.adapter.MyMoreOptionsMenuItemRecyclerViewAdapter
import com.starsearth.one.domain.MoreOptionsMenuItem
import java.util.*
import android.support.v7.widget.DividerItemDecoration
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.starsearth.one.BuildConfig
import com.starsearth.one.activity.KeyboardActivity
import com.starsearth.one.activity.TaskDetailActivity
import com.starsearth.one.activity.profile.PhoneNumberActivity
import com.starsearth.one.activity.welcome.WelcomeOneActivity
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
class MoreOptionsMenuItemFragment : Fragment() {
    // TODO: Customize parameters
    private var mColumnCount = 1
    private var mListener: OnMoreOptionsListFragmentInteractionListener? = null

    fun sendAnalytics(selected: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected)
        val application = (activity?.application as StarsEarthApplication)
        application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun listItemSelected(item: MoreOptionsMenuItem) {
        sendAnalytics(item.text1)
        val intent: Intent
        val title = item.text1
        if (title != null && title.contains("Keyboard")) {
            intent = Intent(context, KeyboardActivity::class.java)
            startActivity(intent)
        } else if (title != null && title.contains("Phone")) {
            intent = Intent(context, PhoneNumberActivity::class.java)
            startActivity(intent)
        } else if (title != null && title.contains("Logout")) {
            FirebaseAuth.getInstance().signOut();
            activity?.finish()
            intent = Intent(context, WelcomeOneActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(context, TaskDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putString("action", if (item.text1.contains("mathematics")) {
                "mathematics"
            } else {
                item.text1
            })
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mColumnCount = arguments!!.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_moreoptionsmenuitem_list, container, false)

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
            val menuItems = getData()
            view.adapter = MyMoreOptionsMenuItemRecyclerViewAdapter(menuItems, mListener, this)
        }
        return view
    }

    fun getData(): ArrayList<MoreOptionsMenuItem> {
        val dataList = ArrayList<String>()
        //dataList.addAll(ArrayList(Arrays.asList(*resources.getStringArray(R.array.se_actions_list))))
        dataList.addAll(Arrays.asList(*resources.getStringArray(R.array.se_keyboard_test_list)))
        dataList.addAll(Arrays.asList(*resources.getStringArray(R.array.se_user_account_list)))
        if (BuildConfig.DEBUG) {
            dataList.add(resources.getString(R.string.logout))
        }
        val menuItems = ArrayList<MoreOptionsMenuItem>()
        for (data in dataList) {
            val item = MoreOptionsMenuItem(data)
            menuItems.add(item)
        }
        return menuItems
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnMoreOptionsListFragmentInteractionListener) {
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
    interface OnMoreOptionsListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onMoreOptionsListFragmentInteraction(item: MoreOptionsMenuItem)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        fun newInstance(columnCount: Int): MoreOptionsMenuItemFragment {
            val fragment = MoreOptionsMenuItemFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}
