package com.starsearth.one.fragments.lists

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.starsearth.one.R
import com.starsearth.one.activity.MainActivity
import com.starsearth.one.adapter.CoronaHelpRequestsRecyclerViewAdapter
import com.starsearth.one.domain.HelpRequest
import com.starsearth.one.domain.SEAddress
import com.starsearth.one.domain.User
import com.starsearth.one.managers.FirebaseManager

import kotlinx.android.synthetic.main.fragment_coronahelprequests_list.*
import kotlinx.android.synthetic.main.fragment_coronahelprequests_list.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [CoronaHelpRequestsFragment.OnListFragmentInteractionListener] interface.
 */
class CoronaHelpRequestsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    // TODO: Customize parameters
    private var columnCount = 1

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mContext : Context
    private lateinit var mSelectedLocality : String
    private var mSelectedSubLocality : String? = null //Used for the dropdown
    private var mSelectedDateMillis : Long = -1
    private var mSpinnerArrayAdapter : ArrayAdapter<Any>? = null
    private var mCopyOfUser : User? = null
    private var mSubLocalities : LinkedHashMap<String, Int> = LinkedHashMap()
    private var listener: OnListFragmentInteractionListener? = null

    private val mHelpRequestsListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            llPleaseWait?.visibility = View.GONE
            var isListEmpty = true
            val map = dataSnapshot?.value
            if (map != null) {
                //First clear the list so we can repopulate
                ((view?.list as RecyclerView)?.adapter as CoronaHelpRequestsRecyclerViewAdapter).removeAllItems()
                for ((key, value) in mSubLocalities) {
                    mSubLocalities[key] = 0 //Resettings all counts to 0
                }
                for (entry in (map as HashMap<*, *>).entries) {
                    val key = entry.key as String
                    val value = entry.value as HashMap<String, Any>
                    var newHelpRequest = HelpRequest(key, value)

                    // Check if date is same as the selected date
                    if (!isDateMatching(newHelpRequest.timestamp)) {
                        Log.d("TAG", "********DATE NOT MATCHING************")
                        continue
                    }
                    if (newHelpRequest.status != "ACTIVE") {
                        Log.d("TAG", "********NOT ACTIVE************")
                        //If it belongs to the same area but is not active, let it go
                        continue
                    }

                    // Keep a record of the admin area. Will be needed to pupulate the dropdown
                    var currentCount : Int = mSubLocalities[newHelpRequest.address.subLocality] ?: 0
                    currentCount = currentCount + 1
                    mSubLocalities.put(newHelpRequest.address.subLocality, currentCount)

                    if (mSelectedSubLocality == null) {
                        Log.d(TAG, "************THREE**************")
                        mSelectedSubLocality = newHelpRequest.address.subLocality
                    } //If it has no value, we will give it the first value
                    if (mSelectedSubLocality != newHelpRequest.address.subLocality) {
                        Log.d("TAG", "********NOT SAME LOCALITY************"+mSelectedSubLocality)
                        //Not the same locality. We do not want to save the HelpRequest in a local data structure
                        continue
                    }

                    isListEmpty = false
                    ((view?.list as RecyclerView)?.adapter as CoronaHelpRequestsRecyclerViewAdapter).addItem(newHelpRequest)
                }
                ((view?.list as RecyclerView)?.adapter as CoronaHelpRequestsRecyclerViewAdapter).notifyDataSetChanged()
                (view?.list as RecyclerView)?.layoutManager?.scrollToPosition(0)
                if (isListEmpty) {
                    list?.visibility = View.GONE
                    tvEmptyList?.visibility = View.VISIBLE
                    getLastLocation() //As we do not have any requests to display, just use the user's default location
                }
                else {
                    list?.visibility = View.VISIBLE
                    tvEmptyList?.visibility = View.GONE
                }

            }
            else {
                list?.visibility = View.GONE
                tvEmptyList?.visibility = View.VISIBLE
                getLastLocation() //As we do not have any requests to display, just use the user's default location
            }

            mSpinnerArrayAdapter?.clear()
            var indexOfSelected = -1
            for ((key, value) in mSubLocalities) {
                println("$key = $value")
                val str = key + "(" + value + ")"
                mSpinnerArrayAdapter?.add(str)
                indexOfSelected++
                Log.d(TAG, "*******SELECTED SUBLOCALITY IS: "+mSelectedSubLocality)
                if (key == mSelectedSubLocality) spinnerLocality?.setSelection(indexOfSelected) //The selected sub locality does not change. Therefore we need to find its position in the list and set it as selected
            }
            mSpinnerArrayAdapter?.notifyDataSetChanged()
        }

        override fun onCancelled(p0: DatabaseError?) {
            llPleaseWait?.visibility = View.GONE
        }

    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (mSelectedSubLocality != null) {
                //We really dont need this
                return
            }
            Log.d("TAG", " ********** LOCATION CALLBACK ***********")
            val location = locationResult.lastLocation
            location?.let {
                getAddressFromLocation(it).get(0)?.let {
                    Log.d(TAG, "************ONE**************")
                    mSelectedSubLocality = it.subLocality as String
                    mSelectedSubLocality?.let {
                        mSubLocalities[it] = mSubLocalities[it] ?: 1
                        mSpinnerArrayAdapter?.add(it)
                        mSpinnerArrayAdapter?.notifyDataSetChanged()
                    }
                    mSelectedLocality = it.locality
                    //loadHelpRequests(mSelectedLocality) //This call is not needed as the spinner.add() call above triggers the loadHelpRequests calls
                }
            }
        }
    }

    private fun isDateMatching(timeMillis : Long) : Boolean {
        val selectedDate = Calendar.getInstance()
        selectedDate.timeInMillis = mSelectedDateMillis
        val downloadedDate = Calendar.getInstance()
        downloadedDate.timeInMillis = timeMillis
        return selectedDate.get(Calendar.YEAR) == downloadedDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.MONTH) == downloadedDate.get(Calendar.MONTH)
                && selectedDate.get(Calendar.DATE) == downloadedDate.get(Calendar.DATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)

        mSelectedDateMillis = Calendar.getInstance().timeInMillis

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            mCopyOfUser = it.getParcelable(ARG_USER)

            if ((mContext as? MainActivity)?.mUser?.uid == mCopyOfUser?.uid) {
                setHasOptionsMenu(true)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_coronahelprequests_list, container, false)

        // Set the adapter
        if (view.list is RecyclerView) {
            with(view.list) {
                (view.list as RecyclerView).layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.list.addItemDecoration(DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL))
                val mainList : ArrayList<HelpRequest> = ArrayList()
                (view.list as RecyclerView).adapter = CoronaHelpRequestsRecyclerViewAdapter(mainList, listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSpinnerArrayAdapter = ArrayAdapter(mContext,android.R.layout.simple_spinner_item, ArrayList<String>().toMutableList() as List<Any>)
        mSpinnerArrayAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocality.setAdapter(mSpinnerArrayAdapter)
        spinnerLocality?.onItemSelectedListener = this

        btnDate?.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = mSelectedDateMillis
            val day = calendar.get(Calendar.DAY_OF_MONTH);
            val month = calendar.get(Calendar.MONTH);
            val year = calendar.get(Calendar.YEAR);
            // date picker dialog
            val picker = DatePickerDialog(mContext,
                        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                            val cal2 = Calendar.getInstance()
                            cal2.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            cal2.set(Calendar.MONTH, monthOfYear)
                            cal2.set(Calendar.YEAR, year)
                            mSelectedDateMillis = cal2.timeInMillis
                            val dateFormat = SimpleDateFormat("dd-MMM-yyyy")
                            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
                            btnDate?.text = dateFormat.format(cal2.time)
                            Log.d(TAG, "*******DATE PICKER*************")
                            mSelectedLocality?.let { loadHelpRequests(it) } //Need to reload the list for the same address to get entries for the new date
                        }, year, month, day);
                picker.show();
        }

        getLastLocation()
        list?.visibility = View.GONE
        btnDate?.text = getFormattedDate(mSelectedDateMillis)
        llPleaseWait?.visibility = View.VISIBLE
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        val selectedItem : String = parent.getItemAtPosition(pos) as String
        var fullText = selectedItem.split("(")
        Log.d(TAG, "************TWO**************")
        mSelectedSubLocality = fullText?.get(0)?.trim()
        Log.d(TAG, "**********ON ITEM SELECTED**********"+mSelectedSubLocality)
        loadHelpRequests(mSelectedLocality)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }


    fun loadHelpRequests(locality: String) {
        Log.d(TAG, "********* LOAD HELP REQUESTS CALLED ************" + locality)
        val firebaseManager = FirebaseManager("help_requests")
        val query = firebaseManager.getQueryForLocation(locality)
        query.addListenerForSingleValueEvent(mHelpRequestsListener)

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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.fragment_help_requests_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.add -> {
                listener?.onCoronaHelpListFragmentAddButtonTapped()
                return true
            }
        }

        return false
    }

    // LOCATION RELATED FUNCTIONS

    //@SuppressLint("MissingPermission")
    fun locationPermissionReceived() {
        requestNewLocationData()
     /*   mFusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known city. In some rare situations this can be null.
                    if (location == null) {
                        requestNewLocationData()
                    }
                    location?.let {
                        getAddressFromLocation(it).get(0)?.let {
                            Log.d("TAG", "******* ADDRESS IS: " + it.subLocality)
                            mAddressFromPhone = SEAddress(it)
                            mSubAdminAreas[mAddressFromPhone!!.subLocality] = mSubAdminAreas[mAddressFromPhone!!.subLocality] ?: 1
                            var list = ArrayList<String>()
                            list.add(mAddressFromPhone!!.subLocality)

                            val aa = ArrayAdapter(mContext,android.R.layout.simple_spinner_item,list.toArray())
                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            spinnerLocality.setAdapter(aa)
                            spinnerLocality.setSelection(0)

                            loadHelpRequests(mAddressFromPhone!!)
                        }
                    }
                }   */

        //val mLocationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager
        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1.0f, mLocationListener);
    }

    private fun getFormattedDate(dateTimeMillis : Long?) : String {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy")
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        val today = Calendar.getInstance().time
        if (dateTimeMillis != null) {
            today.time = dateTimeMillis
        }
        return dateFormat.format(today);
}

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        )
    }

    private fun getAddressFromLocation(location : Location) : List<Address?> {
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(mContext, Locale.getDefault())
        addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1) // Here 1 represent max city result to returned, by documents it recommended 1 to 5
        //tvCity?.text = addresses.size.toString()
        return addresses
    }

    private fun checkPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            true
        } else false
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                locationPermissionReceived()
            } else {
                Toast.makeText(mContext, "Turn on Location in Settings", Toast.LENGTH_LONG).show()
                //val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                //startActivity(intent)
            }
        } else {
            listener?.requestLocationToViewHelpRequests()
        }
    }

    // END OF LOCATION RELATED FUNCTIONS

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
        // TODO: Update argument type and name
        fun onCoronaHelpListFragmentInteraction(item: HelpRequest)
        fun onCoronaHelpListFragmentAddButtonTapped()
        fun requestLocationToViewHelpRequests()
    }

    companion object {

        // TODO: Customize parameter argument names
        val TAG = "CORONA_HELP_REQ_FRAG"
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_USER = "user"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, user : User?) =
                CoronaHelpRequestsFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                        putParcelable(ARG_USER, user)
                    }
                }
    }
}
