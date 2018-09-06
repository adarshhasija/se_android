package com.starsearth.one.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.starsearth.one.R
import com.starsearth.one.activity.tasks.TaskActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.database.Firebase
import com.starsearth.one.domain.*
import kotlinx.android.synthetic.main.fragment_task_detail.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TaskDetailFragment.OnTaskDetailFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TaskDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TaskDetailFragment : Fragment(), View.OnTouchListener {

    private var x1: Float = 0.toFloat()
    private var x2:Float = 0.toFloat()
    private var y1:Float = 0.toFloat()
    private var y2:Float = 0.toFloat()
    private var actionDownTimestamp : Long = 0
    internal val MIN_DISTANCE = 150
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event?.getX()
                y1 = event?.getY()
                actionDownTimestamp = Calendar.getInstance().timeInMillis
            }
            MotionEvent.ACTION_UP -> {
                val actionUpTimestamp = Calendar.getInstance().timeInMillis
                x2 = event?.getX()
                y2 = event?.getY()
                val deltaX = x2 - x1
                val deltaY = y2 - y1
                if (Math.abs(deltaX) > MIN_DISTANCE || Math.abs(deltaY) > MIN_DISTANCE) {
                    gestureSwipe(view)
                } else if (Math.abs(actionUpTimestamp - actionDownTimestamp) > 500) {
                    gestureLongPress(view)
                } else {
                    gestureTap(view)
                }
            }
        }
        return true
    }



    private fun gestureTap(view: View?) {
        if (tvTapScreenToStart.visibility == View.VISIBLE) {
            generateAd()
            if (mTeachingContent is Task) {
                startTask((mTeachingContent as Task))
                sendAnalytics(mTeachingContent, view, FirebaseAnalytics.Event.SELECT_CONTENT)
            }
            else if (mTeachingContent is Course) {
                sendAnalytics(mTeachingContent, view, FirebaseAnalytics.Event.SELECT_CONTENT)
                if (mResults.isEmpty()) {
                    startTask((mTeachingContent as Course).tasks[0])
                }
                else if (!(mTeachingContent as Course).isCourseComplete(mResults)) {
                    val task = (mTeachingContent as Course).getNextTask(mResults)
                    startTask(task)
                }
            }
        }

    }

    private fun gestureLongPress(view: View?) {
        if (tvLongPressForMoreOptions.visibility == View.VISIBLE) {
            mListener?.onTaskDetailFragmentLongPressInteraction(mTeachingContent, mResults)
            sendAnalytics(mTeachingContent, view, "LONG_PRESS")
        }
    }

    private fun gestureSwipe(view: View?) {
        if (tvSwipeToContinue.visibility == View.VISIBLE) {
            if (mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                mListener?.onTaskDetailFragmentSwipeInteraction(mTeachingContent)
                sendAnalytics(mTeachingContent, view, "SWIPE")
            }
        }
    }

    // TODO: Rename and change types of parameters
    private var mTeachingContent: Any? = null
    private var mResults: ArrayList<Result> = ArrayList()
    private var mReturnBundle = Bundle()

    private var mListener: OnTaskDetailFragmentInteractionListener? = null
    private var adRequest: AdRequest.Builder? = null
    private var mDatabase : DatabaseReference? = null


    /******* ADS LISTENERS ****************/
    private var isAdAvailable = false

    private val mGoogleAdListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            isAdAvailable = true
        }

        override fun onAdClicked() {
            super.onAdClicked()
        }

        override fun onAdFailedToLoad(p0: Int) {
            super.onAdFailedToLoad(p0)
        }

        override fun onAdClosed() {
            super.onAdClosed()
            isAdAvailable = false
        }

        override fun onAdOpened() {
            super.onAdOpened()
        }

    }

    private val mFacebookAdListener = object : InterstitialAdListener {
        override fun onInterstitialDisplayed(ad: Ad) {
            // Interstitial displayed callback
        }

        override fun onInterstitialDismissed(ad: Ad) {
            // Interstitial dismissed callback
            isAdAvailable = false
        }

        override fun onError(ad: Ad, adError: AdError) {
            // Ad error callback
            //Toast.makeText(this@MainActivity, "Error: " + adError.errorMessage,Toast.LENGTH_LONG).show()
        }

        override fun onAdLoaded(ad: Ad) {
            // Show the ad when it's done loading.
            isAdAvailable = true
        }

        override fun onAdClicked(ad: Ad) {
            // Ad clicked callback
        }

        override fun onLoggingImpression(ad: Ad) {
            // Ad impression logged callback
        }
    }

    fun showAd() {
        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().ads
        if (ads == "Google") {
            (activity?.application as StarsEarthApplication)?.googleInterstitialAd.show()
        }
        else if (ads == "Facebook") {
            (activity?.application as StarsEarthApplication)?.facebookInterstitalAd.show()
        }
    }

    fun setupAdListener() {
        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().ads
        if (ads == "Google") {
            (activity?.application as StarsEarthApplication)?.googleInterstitialAd.adListener = mGoogleAdListener
        }
        else if (ads == "Facebook") {
            (activity?.application as StarsEarthApplication)?.facebookInterstitalAd.setAdListener(mFacebookAdListener)
        }
    }

    fun generateAd() {
        val isAccessibilityUser = (activity?.application as StarsEarthApplication).accessibility.isAccessibilityUser
        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().ads
        val isOwnerWantingAds = if (mTeachingContent is Task) {
            (mTeachingContent as Task).isOwnerWantingAds
        } else {
            false
        }
        //only generate ads for non-accessibility users
        //only generate ads if task owner wants to make money from ads
        if (ads != "None" && !isAccessibilityUser && isOwnerWantingAds) {
            val moduloString = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().adsFrequencyModulo
            val moduloInt = Integer.parseInt(moduloString)
            val random = Random()
            val shouldGenerateAd = if (moduloInt > 0 && random.nextInt(moduloInt) % moduloInt == 0) {
                true
            }
            else {
                false
            }
            if (shouldGenerateAd) {
                if (ads == "Google" && adRequest != null) {
                    (activity?.application as StarsEarthApplication)?.googleInterstitialAd.loadAd(adRequest?.build())
                }
                else if (ads == "Facebook") {
                    //AdSettings.addTestDevice("b8441b0c-b48d-4d5e-8d36-c67770d5bf01"); //TS Mac simulator
                    //AdSettings.addTestDevice("c2d5b02b-abe8-4901-bc66-226c06250599"); //AH Mac simulator
                    (activity?.application as StarsEarthApplication)?.facebookInterstitalAd.loadAd()
                }
            }
        }
    }
    /****************************************/


    /**** CHILD EVENT LISTENERS *************/
    private val mChildEventListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError?) {
        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
          /*  val result = if ((mTeachingContent as Task)?.type == Task.Type.TYPING) {
                dataSnapshot?.getValue(ResultTyping::class.java)
            } else {
                dataSnapshot?.getValue(Result::class.java)
            }
            if ((mTeachingContent as SEBaseObject)?.id != result!!.task_id) {
                return;
            }
            view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.visibility = View.VISIBLE
            //do not want to call announce for accessibility here. Only set content description
            view?.findViewById<LinearLayout>(R.id.ll_main)?.contentDescription = getContentDescriptionForAccessibility()

            if (mResults.empty() || !isResultExistsInStack(result)) {
                mResults.push(result)
                //if (result.isJustCompleted) {mListener?.onTaskDetailFragmentShowLastTried(mTeachingContent, result, null, null)}
            }   */
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
        }
    }

    /**************************/

    private fun setReturnResult(result: Parcelable) {
        val intent = Intent()
        //bundle.putString("uid", (result as Result)?.uid)
        if (mReturnBundle.getParcelableArrayList<Parcelable>("RESULTS") == null) {
            mReturnBundle.putParcelableArrayList("RESULTS", ArrayList())
        }
        mReturnBundle.getParcelableArrayList<Parcelable>("RESULTS")?.add(result)
        intent.putExtras(mReturnBundle)
        activity?.setResult(Activity.RESULT_OK, intent)
    }


    fun analyticsTaskCompleted(task: Task, result: Any?) {
        val bundle = Bundle()
        bundle.putLong(FirebaseAnalytics.Param.ITEM_ID, task.id)

        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.title)

        if (task is Task) {
            bundle.putInt("item_type", task.getType().getValue().toInt())
            bundle.putInt("item_timed", if (task.timed) { 1 } else { 0 })
            bundle.putInt("item_submit_on_enter_tapped", if (task.submitOnReturnTapped) { 1 } else { 0 })
            bundle.putInt("item_is_text_visible_on_start", if (task.isTextVisibleOnStart) { 1 } else { 0 })
            bundle.putInt(FirebaseAnalytics.Param.SCORE, (result as Result).items_correct)
        }

        val application = (activity?.application as StarsEarthApplication)
        val score = bundle?.getInt(FirebaseAnalytics.Param.SCORE)
        application.logActionEvent(FirebaseAnalytics.Event.POST_SCORE, bundle, score)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTeachingContent = arguments?.getParcelable(ARG_TEACHING_CONTENT)
            val parcelableArrayList = arguments?.getParcelableArrayList<Parcelable>(ARG_RESULTS)
            if (parcelableArrayList != null) {
                for (item in parcelableArrayList) {
                    mResults.add((item as Result))
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_task_detail, container, false)

        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().ads
        if (ads == "Google") {
            adRequest = AdRequest.Builder()
            if (mTeachingContent is Task) {
                val tags = (mTeachingContent as Task).tags
                for (tag in tags) {
                    adRequest?.addKeyword(tag)
                }
            }
        }

        if (mResults != null && !mResults.isEmpty()) {
            //UI changes if there are exisitng results
            view?.findViewById<TextView>(R.id.tvLongPressForMoreOptions)?.visibility = View.VISIBLE
            //do not want to call announce for accessibility here. Only set content description
            view?.findViewById<LinearLayout>(R.id.llMain)?.contentDescription = getContentDescriptionForAccessibility()
        }


        val currentUser = FirebaseAuth.getInstance().currentUser
        mDatabase = FirebaseDatabase.getInstance().getReference("results")
        mDatabase?.keepSynced(true)
        val query = mDatabase?.orderByChild("userId")?.equalTo(currentUser!!.uid)
        //query?.addChildEventListener(mChildEventListener);

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdListener()
        view.findViewById<LinearLayout>(R.id.llMain).setOnTouchListener(this)
        //setupScreenAccessibility()
        updateUIVisibility() //Must be called from here as view exists from here
        updateUIText() //Must be called from here as view exists from here
        llMain?.contentDescription = getContentDescriptionForAccessibility()
        announceForAccessibility()
    }

    fun updateUIVisibility() {
        //Set visibility for all UI
        tvInstruction?.visibility = View.VISIBLE
        tvProgress?.visibility =
                if ((mTeachingContent is Course) && (mTeachingContent as Course).isFirstTaskPassed(mResults)) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvSingleTapToRepeat?.visibility =
                if ((activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvTapScreenToStart?.visibility =
                if (mTeachingContent is Course && (mTeachingContent as Course).isCourseComplete(mResults)) {
                    View.GONE
                }
                else {
                    View.VISIBLE
                }
        tvSwipeToContinue?.visibility =
                if (mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvLongPressForMoreOptions?.visibility =
                if (mTeachingContent is Course || mTeachingContent is Task && mResults.isNotEmpty()) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
    }

    fun updateUIText() {
        tvProgress?.text =
                if (mTeachingContent is Course && (mTeachingContent as Course).isCourseComplete(mResults)) {
                    context?.resources?.getText(R.string.complete)
                }
                else if (mTeachingContent is Course && (mTeachingContent as Course).isCourseStarted(mResults)){
                    val nextTaskIndex = (mTeachingContent as Course).getCurrentTaskIndex(mResults) + 2
                    context?.resources?.getString(R.string.next_task) + "\n" + nextTaskIndex + "/" + (mTeachingContent as Course).tasks.size
                }
                else {
                    //If the course has not started, or if teachingContent is a Task, empty string
                    ""
                }

        val tv = tvInstruction
        var instructions =
                if (mTeachingContent is Course && !(mTeachingContent as Course).isCourseComplete(mResults)) {
                    //Get the instructions of next task
                    (mTeachingContent as Course).getNextTask(mResults)?.instructions
                }
                else {
                    //Course or Task, get the normal instructions
                    (mTeachingContent as SEBaseObject)?.instructions
                }
        if (mTeachingContent is Task) {
            if ((mTeachingContent as Task)?.durationMillis > 0) {
                instructions +=
                        context?.resources?.getString(R.string.complete_as_many_as) +
                        " " + (mTeachingContent as Task)?.getTimeLimitAsString(context)
            }
            if ((mTeachingContent as Task).isExitOnInterruption) {
                instructions += context?.resources?.getString(R.string.activity_will_end_if_interrupted)
            }
        }
        (tv as TextView).text = instructions

        tvTapScreenToStart?.text =
                if ((activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn) {
                    context?.resources?.getString(R.string.double_tap_screen_to_start)
                }
                else {
                    context?.resources?.getString(R.string.tap_screen_to_start)
                }

        tvSwipeToContinue?.text =
                if ((activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn && mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                    context?.resources?.getString(R.string.swipe_with_2_fingers_for_keyboard_test)
                }
                else {
                    context?.resources?.getString(R.string.swipe_for_keyboard_test)
                }

        tvLongPressForMoreOptions?.text =
                if ((activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn) {
                    context?.resources?.getString(R.string.tap_and_long_press_for_more_options)
                }
                else {
                    context?.resources?.getString(R.string.long_press_for_more_options)
                }

        llMain?.contentDescription = getContentDescriptionForAccessibility() //set for accessibility
    }

    fun announceForAccessibility() {
        var contentDescription = getContentDescriptionForAccessibility()
        view?.findViewById<LinearLayout>(R.id.llMain)?.contentDescription = contentDescription
        view?.announceForAccessibility(contentDescription)
    }

    fun setGesturesText() {
        val isTalkbackOn = (activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn

        if (isTalkbackOn) {
            tvSingleTapToRepeat?.visibility = View.VISIBLE
            tvSwipeToContinue?.text = context?.resources?.getString(R.string.swipe_with_2_fingers_for_keyboard_test)
            tvLongPressForMoreOptions?.text = context?.resources?.getString(R.string.tap_and_long_press_for_more_options)
            tvTapScreenToStart?.text = context?.resources?.getString(R.string.double_tap_screen_to_start)
        }
        else {
            tvSingleTapToRepeat?.visibility = View.GONE
            tvSwipeToContinue?.text = context?.resources?.getText(R.string.swipe_for_keyboard_test)
            tvLongPressForMoreOptions?.text = context?.resources?.getString(R.string.long_press_for_more_options)
            tvTapScreenToStart?.text = context?.resources?.getString(R.string.tap_screen_to_start)
        }
    }

    fun getContentDescriptionForAccessibility() : String {
        val isTalkbackOn = (activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn

        var contentDescription = tvInstruction?.text.toString()
        if (isTalkbackOn) {
            contentDescription += tvSingleTapToRepeat?.text.toString()
        }

        contentDescription += " " + tvTapScreenToStart?.text.toString()
        if (tvSwipeToContinue?.visibility == View.VISIBLE) {
            contentDescription += " " + tvSwipeToContinue?.text.toString()
        }
        if (tvLongPressForMoreOptions?.visibility == View.VISIBLE) {
            contentDescription += " " + tvLongPressForMoreOptions?.text.toString()
        }

        return contentDescription
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            val extras = data?.extras
            val reason = extras?.get("reason")
            if (reason != null) {
                if (reason == "no attempt") {
                    //Toast.makeText(context, R.string.cancelled_no_attempt, Toast.LENGTH_LONG).show()
                    mListener?.onTaskDetailFragmentShowLastTried(null, null, getString(R.string.cancelled), getString(R.string.no_attempt))
                }
                else if (reason == "gesture spam") {
                    val alertDialog = (activity?.application as StarsEarthApplication).createAlertDialog(context)
                    alertDialog.setTitle(getString(R.string.gesture_spam_detected))
                    alertDialog.setMessage((activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().gestureSpamMessage)
                    alertDialog.setCancelable(false)
                    alertDialog.setPositiveButton(getString(android.R.string.ok), null)
                    alertDialog.show()

                    //mListener?.onTaskDetailFragmentShowLastTried(null, null, getString(R.string.gesture_spam_detected), message)
                }
            }
            else {
                //Show cancelled message
                mListener?.onTaskDetailFragmentShowLastTried(null, null, getString(R.string.cancelled), getString(R.string.typing_game_cancelled))
            }

        }
        else if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                taskComplete(data.extras)
            }
            if (isAdAvailable == true) {
                showAd()
            }
        }
    }

    private fun taskComplete(bundle: Bundle) {
        val firebase = Firebase("results")
        val resultMap : HashMap<String, Any> = bundle.getSerializable("result_map") as HashMap<String, Any>
        val type = Task.Type.fromInt((resultMap["taskTypeLong"] as Long)) //Task.Type.fromInt(bundle.getLong("taskTypeLong"))
        val result =
                if (type == Task.Type.TYPING) {
                    firebase.writeNewResultTyping(resultMap)
                } else {
                    firebase.writeNewResult(resultMap)
                }

        newResultProcedures(result)
        updateUIVisibility()
        updateUIText()
    }

    private fun newResultProcedures(result: Result) {
        //1. Do analytics
        analyticsTaskCompleted(if(mTeachingContent is Task) {
            mTeachingContent as Task
        } else {
            (mTeachingContent as Course).getTaskById(result.task_id)
        }, result)

        //2. Set return result to previous screen onActivityResult
        setReturnResult(result)

        //3. Update the Fragment's mResults array
        if (result != null) {
            mResults?.add(result)
        }

        //4. Show the results screen to the user
        mListener?.onTaskDetailFragmentShowLastTried(if (mTeachingContent is Task) {
            mTeachingContent as Task
        } else {
            (mTeachingContent as Course).getTaskById(result.task_id)
        }, result, null, null)
    }

    private fun sendAnalytics(teachingContent: Any?, view: View?, action: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Screen")
        if (teachingContent is Task) {
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, teachingContent.type?.toString()?.replace("_", " "))
            bundle.putString("content_name", teachingContent.title)
            bundle.putInt("content_timed", if (teachingContent.timed) { 1 } else { 0 })
        }
        val application = (activity?.application as StarsEarthApplication)
        application.logActionEvent(action, bundle)
        //mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    override fun onResume() {
        super.onResume()
        val application = (activity?.application as StarsEarthApplication)
        application.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
        //mFirebaseAnalytics?.setCurrentScreen(activity!!, this.javaClass.name, null /* class override */);
    }

    private fun startTask(task: Task) {
        val intent = Intent(context, TaskActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable("task", task)
        intent.putExtras(bundle)
        startActivityForResult(intent, 0)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnTaskDetailFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnTaskDetailFragmentInteractionListener")
        }
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
    interface OnTaskDetailFragmentInteractionListener {
        fun onTaskDetailFragmentSwipeInteraction(teachingContent: Any?)
        fun onTaskDetailFragmentLongPressInteraction(teachingContent: Any?, results: ArrayList<Result>)
        fun onTaskDetailFragmentShowLastTried(teachingContent: Any?, result: Any?, title: String?, message: String?)
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_TEACHING_CONTENT = "TEACHING_CONTENT"
        private val ARG_RESULTS = "RESULTS"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TaskDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(teachingContent: Parcelable?, results: ArrayList<Parcelable>?): TaskDetailFragment {
            val fragment = TaskDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, teachingContent)
            args.putParcelableArrayList(ARG_RESULTS, results)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
