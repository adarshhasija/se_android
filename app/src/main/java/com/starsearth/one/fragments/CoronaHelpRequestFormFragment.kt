package com.starsearth.one.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.starsearth.one.R
import com.starsearth.one.activity.MainActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.HelpRequest
import com.starsearth.one.domain.SEAddress
import kotlinx.android.synthetic.main.fragment_corona_help_request_form.*
import kotlinx.android.synthetic.main.fragment_coronahelprequests.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_HELP_REQUEST = "help_request"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CoronaHelpRequestFormFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CoronaHelpRequestFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CoronaHelpRequestFormFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var mContext : Context
    private var param1: String? = null
    private var param2: String? = null
    private var mHelpRequest : HelpRequest? = null
    private var mAddressFromPhone : SEAddress? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var listener: OnFragmentInteractionListener? = null

    private val mLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location?) {
            p0?.let {
                getAddressFromLocation(it).get(0)?.let {
                    mAddressFromPhone = SEAddress(it)
                    val addressLine = it.getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    val city = it.locality
                    val state = it.adminArea
                    val country = it.countryName
                    val postalCode = it.postalCode
                    val knownName = it.featureName // Only if available else return NULL
                    tvSublocality?.text = it.subLocality
                }
            }
        }

        override fun onProviderDisabled(p0: String?) {
            Log.d(TAG, "******** ON PROVIDER DISABLED *************")
        }

        override fun onProviderEnabled(p0: String?) {
            Log.d(TAG, "******** ON PROVIDER ENABLED **************")
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            Log.d(TAG, "********* ON STATUS CHANGED ***************")
        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d("TAG", " ********** LOCATION CALLBACK ***********"+ locationResult)
            val mLastLocation = locationResult.lastLocation
            //tvCity?.text = mLastLocation.latitude.toString() + " " + mLastLocation.longitude.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)

        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
            //param2 = it.getString(ARG_PARAM2)
            mHelpRequest = it.getParcelable(ARG_HELP_REQUEST)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_corona_help_request_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mHelpRequest != null) {
            tvPhoneNumber?.text = mHelpRequest!!.phone
            tvSublocality?.text = mHelpRequest!!.address.addressLine + "\n" + mHelpRequest!!.address.locality + "\n" + mHelpRequest!!.address.adminArea + "\n" + mHelpRequest!!.address.countryName + "\n" + mHelpRequest!!.address.postalCode
            etName?.visibility = View.GONE
            tvName?.text = mHelpRequest!!.name
            etLandmark?.visibility = View.GONE
            tvLandmarkEnetered?.visibility = View.VISIBLE
            tvLandmarkEnetered?.text = mHelpRequest!!.landmark
            tvSelectedRequest?.text = mHelpRequest!!.request
            tvSelectedRequest?.visibility = View.VISIBLE
            spinnerRequest?.visibility = View.GONE
            btnSubmit?.visibility = View.GONE
            btnMap?.visibility = View.VISIBLE
            btnComplete?.visibility = View.VISIBLE

            btnMap?.setOnClickListener {
                val uri = String.format(Locale.ENGLISH, "geo:%f,%f", mHelpRequest!!.address.latitude, mHelpRequest!!.address.longitude);
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                mContext.startActivity(intent)
            }

            btnComplete?.setOnClickListener {
                val alertDialog = (activity?.application as StarsEarthApplication)?.createAlertDialog(mContext)
                alertDialog.setTitle("Is this complete?")
                //alertDialog.setMessage(getString(R.string.responses_not_recorded))
                alertDialog.setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()

                    val mDatabase = FirebaseDatabase.getInstance().getReference("help_requests/" + mHelpRequest!!.uid)
                    mDatabase.child("status").setValue("COMPLETE").addOnSuccessListener {
                        listener?.requestCompleted()
                    }.addOnFailureListener {
                        val alertDialog2 = (activity?.application as StarsEarthApplication)?.createAlertDialog(mContext)
                        alertDialog2.setTitle("Error")
                        alertDialog2.setMessage("Failed to save. Please try again")
                        alertDialog2.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                        alertDialog2.show()
                    }
                })
                alertDialog.setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                alertDialog.show()

            }

            return
        }

        getLastLocation()
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        phoneNumber?.let {
            tvPhoneNumber?.text = phoneNumber
        }
        val userName = (activity as? MainActivity)?.mUser?.name
        if (userName.isNullOrEmpty()) {
            tvNameLabel.visibility = View.GONE
            tvName.visibility = View.GONE
            etName.visibility = View.VISIBLE
        }
        else {
            tvNameLabel.visibility = View.VISIBLE
            tvName.visibility = View.VISIBLE
            tvName.text = userName
            etName.visibility = View.GONE
        }
        val spinnerList = ArrayList<String>()
        spinnerList.add("Food")
        spinnerList.add("Groceries")
        spinnerList.add("Medical")

        //Creating the ArrayAdapter instance having the country list
        val aa = ArrayAdapter(mContext,android.R.layout.simple_spinner_item,spinnerList.toArray())
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinnerRequest.setAdapter(aa);
        spinnerRequest.setSelection(0)

        btnSubmit?.setOnClickListener {
            val userName = (activity as? MainActivity)?.mUser?.name
            val name = if (userName.isNullOrEmpty()) {
                tvName?.text.toString().toUpperCase()
                }
                else {
                    userName
                }
            val city = tvSublocality?.text.toString().toUpperCase()
            val landmark = etLandmark?.text.toString()
            val request = (spinnerRequest?.selectedItem as String).toUpperCase()

            val mDatabase = FirebaseDatabase.getInstance().getReference()
            val key: String = mDatabase.push().getKey()
            val userId = (activity as? MainActivity)?.mUser?.uid
            val map = HashMap<String, Any>()
            map.put("uid", key)
            map.put("userId", userId!!)
            phoneNumber?.let { map.put("phone", it) }
            map.put("name", name)
            mAddressFromPhone?.let { map.put("address", it) }
            map.put("landmark", landmark)
            map.put("request", request)
            map.put("status", "ACTIVE")
            map["timestamp"] = ServerValue.TIMESTAMP //testResult has local timestamp, values has sever timestamp

            val childUpdates: MutableMap<String, Any> = HashMap()
            childUpdates["help_requests/"+key] = map
            if (userName.isNullOrBlank()) {
                //User had not set username before. Should save it now for future convinience
                (activity as? MainActivity)?.mUser?.name = name.toUpperCase()
                childUpdates["users/"+userId+"/name"] = name.toUpperCase()
            }

            llPleaseWait?.visibility = View.VISIBLE
            mDatabase.updateChildren(childUpdates).addOnSuccessListener {
                llPleaseWait?.visibility = View.GONE
                listener?.onNewHelpRequestMade()
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mContext = context
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    //@SuppressLint("MissingPermission")
    fun locationPermissionReceived() {
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        mFusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known city. In some rare situations this can be null.
                    if (location == null) {
                        requestNewLocationData()
                    }
                    location?.let {
                        getAddressFromLocation(it).get(0)?.let {
                            mAddressFromPhone = SEAddress(it)
                            val addressLine =   it.getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            val city = it.locality
                            val state = it.adminArea
                            val country = it.countryName
                            val postalCode = it.postalCode
                            val knownName = it.featureName // Only if available else return NULL
                            val locality = it.locality
                            val subLocality = it.subLocality // This is the area
                            val premesis = it.premises
                            val subAdminArea = it.subAdminArea
                            tvSublocality?.text = addressLine + "\n" + city + "\n" + state + "\n" + country + "\n" + postalCode
                        }
                    }
                }

        //val mLocationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager
        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1.0f, mLocationListener);
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
            listener?.requestLocationForHelpRequest()
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onNewHelpRequestMade()
        fun requestLocationForHelpRequest()
        fun requestCompleted()
    }

    companion object {

        val TAG = "HELP_REQ_FORM_FRAGMENT"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CoronaHelpRequestFormFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                CoronaHelpRequestFormFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }

        @JvmStatic
        fun newInstance(helpRequest: HelpRequest) =
                CoronaHelpRequestFormFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_HELP_REQUEST, helpRequest)
                    }
                }
    }
}
