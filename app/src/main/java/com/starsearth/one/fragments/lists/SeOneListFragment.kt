package com.starsearth.one.fragments.lists

import android.content.Context
import android.content.Intent
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
import java.util.*
import android.support.v7.widget.DividerItemDecoration
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.starsearth.one.BuildConfig
import com.starsearth.one.AssetsFileManager
import com.starsearth.one.activity.KeyboardActivity
import com.starsearth.one.activity.DetailActivity
import com.starsearth.one.activity.profile.PhoneNumberActivity
import com.starsearth.one.activity.welcome.WelcomeOneActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.Task
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

    fun sendAnalytics(selected: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected)
        val application = (activity?.application as StarsEarthApplication)
        application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun listItemSelected(item: SEOneListItem) {
        sendAnalytics(item.text1)
        val intent: Intent
        val title = item.text1
        if (title != null && title.contains("Keyboard Test")) {
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
            val intent = Intent(context, DetailActivity::class.java)
            val bundle = Bundle()
            bundle.putString("title", item.text1)
            bundle.putParcelable("teachingContent", if (item.text1.contains("Keyboard Typing")) {
                AssetsFileManager.getCourseById(context, 10) //Task id for keyboard typing course
            } else {
                null
            })
            bundle.putString("action", if (item.text1.contains("Mathematics")) {
                "mathematics"
            } else if (item.text1.contains("English")) {
                "english"
            } else if (item.text1.contains("Spelling")) {
                "spelling"
            } else if (item.text1.contains("Typing")) {
                "typing"
            } else if (item.text1.contains("Courses")) {
                "courses"
            } else if (item.text1.contains("General")) {
                "general"
            } else {
                ""
            })
            bundle.putLong("type", if (item.text1.contains("Gesture")) {
                Task.Type.TAP_SWIPE.value
            } else if (item.text1.contains("Spelling")) {
                Task.Type.SPELLING.value
            } else {
                0
            })
            bundle.putBoolean("isTimed", if (item.text1.contains("Timed")) {
                true
            } else {
                false
            })
            bundle.putBoolean("isGame", if (item.text1.contains("Game")) {
                true
            } else {
                false
            })
            if (item.text1.contains("subject")) {
                val subjects = ArrayList<String>()
                subjects.add("General")
                subjects.add("English")
                subjects.add("Mathematics")
                subjects.add("Typing")
                bundle.putStringArrayList("subjects", subjects)
            }
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

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
        val dataList = ArrayList<String>()

        //Add type related items
        if (mType == SEOneListItem.Type.TAG) {
            dataList.addAll(AssetsFileManager.getAllTags(context))
        }

        //Add default items
        //dataList.addAll(ArrayList(Arrays.asList(*resources.getStringArray(R.array.se_actions_list))))
        dataList.addAll(Arrays.asList(*resources.getStringArray(R.array.se_keyboard_test_list)))
        dataList.addAll(Arrays.asList(*resources.getStringArray(R.array.se_user_account_list)))
        if (BuildConfig.DEBUG) {
            dataList.add(resources.getString(R.string.logout))
        }
        val menuItems = ArrayList<SEOneListItem>()
        for (data in dataList) {
            val item = SEOneListItem(data)
            menuItems.add(item)
        }
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
        fun onMoreOptionsListFragmentInteraction(item: SEOneListItem)
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
