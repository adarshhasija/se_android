package com.starsearth.two.fragments

import android.app.Activity
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.starsearth.two.R
import com.starsearth.two.application.StarsEarthApplication
import com.starsearth.two.domain.TagListItem
import com.starsearth.two.domain.User
import com.starsearth.two.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.search_fragment.*
import java.util.*


class SearchFragment : Fragment() {

    companion object {
        val TAG = "SEARCH_FRAG"
        fun newInstance() = SearchFragment()
        fun newInstance(type: String) =
                SearchFragment().apply {
                    arguments = Bundle().apply {
                        putString("TYPE", type)
                    }
                }
    }

    private lateinit var viewModel: SearchViewModel
    private lateinit var mContext: Context
    private var mSearchType : String? = null //This is so that we have 2 different search options on main screen. Can be removed later
    private var mUserResultsFound : Boolean? = null
    private var mTagResultsFound : Boolean? = null
    private var listener: OnFragmentInteractionListener? = null

    private val mEducatorValuesListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val map = snapshot?.value
            if (map != null) {
                val resultsArray = ArrayList<Parcelable>()
                for (entry in (map as HashMap<*, *>).entries) {
                    val key = entry.key as String
                    val value = entry.value as Map<String, Any?>
                    if (value.containsKey("educator") == true && value.get("educator") == "ACTIVE") {
                        val user = User(key, value)
                        resultsArray.add(user)
                    }
                }
                if (resultsArray.size > 0) {
                    mUserResultsFound = true
                    listener?.onSearchResultsObtained(resultsArray, "EDUCATOR")
                }
                else if (mTagResultsFound == false) {
                    //Tags query has already returned with no results. Display error
                    val alertDialog = (activity?.application as? StarsEarthApplication)?.createAlertDialog(mContext)
                    alertDialog?.setTitle(mContext.getString(R.string.error))
                    alertDialog?.setMessage(mContext.getString(R.string.no_search_results))
                    alertDialog?.setPositiveButton(android.R.string.ok, null)
                    alertDialog?.show()
                    mUserResultsFound = null
                    mTagResultsFound = null
                }
                else {
                    //Tags query is yet to return. Simply set the boolean and let the tag listener make the final decision
                    mUserResultsFound = false
                }
            }
            else if (mTagResultsFound == false) {
                //Tags query has already returned with no results. Display error
                val alertDialog = (activity?.application as? StarsEarthApplication)?.createAlertDialog(mContext)
                alertDialog?.setTitle(mContext.getString(R.string.error))
                alertDialog?.setMessage(mContext.getString(R.string.no_search_results))
                alertDialog?.setPositiveButton(android.R.string.ok, null)
                alertDialog?.show()
                mUserResultsFound = null
                mTagResultsFound = null
            }
            else {
                //Tags query is yet to return. Simply set the boolean and let the tag listener make the final decision
                mUserResultsFound = false
            }

            llPleaseWait?.visibility = View.GONE
        }

        override fun onCancelled(error: DatabaseError) {
            llPleaseWait?.visibility = View.GONE
            if (mTagResultsFound == false) {
                val alertDialog = (activity?.application as? StarsEarthApplication)?.createAlertDialog(mContext)
                alertDialog?.setTitle(mContext.getString(R.string.error))
                alertDialog?.setMessage(mContext.getString(R.string.no_search_results))
                alertDialog?.setPositiveButton(android.R.string.ok, null)
                alertDialog?.show()
                mUserResultsFound = null
                mTagResultsFound = null
            }
            else {
                mUserResultsFound = false
            }
        }

    }

    private val mTagValuesListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val map = snapshot?.value
            if (map != null) {
                val resultsArray = ArrayList<Parcelable>()
                for (entry in (map as HashMap<*, *>).entries) {
                    val key = entry.key as String
                    val value = entry.value as Map<String, Any?>
                    //Tag
                    val tag = TagListItem(key, value)
                    resultsArray.add(tag)
                }
                if (resultsArray.size > 0) {
                    mTagResultsFound = true
                    listener?.onSearchResultsObtained(resultsArray, "TAG")
                }
                else if (mUserResultsFound == false) {
                    val alertDialog = (activity?.application as? StarsEarthApplication)?.createAlertDialog(mContext)
                    alertDialog?.setTitle(mContext.getString(R.string.error))
                    alertDialog?.setMessage(mContext.getString(R.string.no_search_results))
                    alertDialog?.setPositiveButton(android.R.string.ok, null)
                    alertDialog?.show()
                    mUserResultsFound = null
                    mTagResultsFound = null
                }
                else {
                    mTagResultsFound = false
                }
            }
            else if (mUserResultsFound == false) {
                val alertDialog = (activity?.application as? StarsEarthApplication)?.createAlertDialog(mContext)
                alertDialog?.setTitle(mContext.getString(R.string.error))
                alertDialog?.setMessage(mContext.getString(R.string.no_search_results))
                alertDialog?.setPositiveButton(android.R.string.ok, null)
                alertDialog?.show()
                mUserResultsFound = null
                mTagResultsFound = null
            }
            else {
                mTagResultsFound = false
            }

            llPleaseWait?.visibility = View.GONE
        }

        override fun onCancelled(error: DatabaseError) {
            llPleaseWait?.visibility = View.GONE
            if (mUserResultsFound == false) {
                val alertDialog = (activity?.application as? StarsEarthApplication)?.createAlertDialog(mContext)
                alertDialog?.setTitle(mContext.getString(R.string.error))
                alertDialog?.setMessage(mContext.getString(R.string.no_search_results))
                alertDialog?.setPositiveButton(android.R.string.ok, null)
                alertDialog?.show()
                mUserResultsFound = null
                mTagResultsFound = null
            }
            else {
                mTagResultsFound = false
            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mSearchType = it.getString("TYPE")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mSearchType.equals("EDUCATOR", true)) {
            tvInstruction?.visibility = View.VISIBLE
            tvInstruction?.text = mContext.getString(R.string.educator_search_hint)
        }
        else if (mSearchType.equals("CLASS", true)) {
            tvInstruction?.visibility = View.VISIBLE
            tvInstruction?.text = mContext.getString(R.string.class_search_hint)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            mContext = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnSearchFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStart() {
        super.onStart()

        btnSubmit?.setOnClickListener {
            if (llPleaseWait?.visibility != View.VISIBLE) {
                //Should only proceed if a search is not currently in progress
                val searchText = etSearch?.text.toString().trim().toUpperCase(Locale.getDefault())
                if (searchText.length > 0) {
                    val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etSearch.windowToken, 0) //Close keyboard

                    llPleaseWait?.visibility = View.VISIBLE
                    val refEducators = FirebaseDatabase.getInstance().getReference("users")
                    val educatorsQuery = refEducators.orderByChild("name").equalTo(searchText)
                    educatorsQuery.addListenerForSingleValueEvent(mEducatorValuesListener)

                    val refTags = FirebaseDatabase.getInstance().getReference("tags")
                    val tagsQuery = refTags.orderByKey().equalTo(searchText)
                    tagsQuery.addListenerForSingleValueEvent(mTagValuesListener)
                }
                else {
                    val alertDialog = (activity?.application as? StarsEarthApplication)?.createAlertDialog(mContext)
                    alertDialog?.setTitle(mContext.getString(R.string.error))
                    alertDialog?.setMessage(mContext.getString(R.string.you_did_not_enter_search))
                    alertDialog?.setPositiveButton(android.R.string.ok, null)
                    alertDialog?.show()
                }
            }

        }

        etSearch?.hint =
                if (mSearchType == "CLASS") {
                    mContext.getString(R.string.search_by_class_hint)
                }
                else {
                    mContext.getString(R.string.enter_educator_name)
                }

        etSearch.postDelayed({
            etSearch?.requestFocus()
            val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(cl, 0)
        }, 500)
    }

    interface OnFragmentInteractionListener {
        fun onSearchResultsObtained(resultsList: ArrayList<Parcelable>, type: String)
    }

}
