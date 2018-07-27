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
        generateAd()
        startTask((mTeachingContent as Task))
        sendAnalytics((mTeachingContent as Task), view, FirebaseAnalytics.Event.SELECT_CONTENT)
    }

    private fun gestureLongPress(view: View?) {
        mListener?.onTaskDetailFragmentLongPressInteraction(mTeachingContent, mResults)
        sendAnalytics((mTeachingContent as Task), view, "LONG_PRESS")
    }

    private fun gestureSwipe(view: View?) {

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
                //dataSnapshot?.getValue(ResultGestures::class.java)
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
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, task.id)

        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.title)

        if (task is Task) {
            bundle.putInt("item_type", task.getType().getValue().toInt())
            bundle.putInt("item_timed", if (task.timed) { 1 } else { 0 })
            bundle.putInt("item_submit_on_enter_tapped", if (task.submitOnReturnTapped) { 1 } else { 0 })
            bundle.putInt("item_is_text_visible_on_start", if (task.isTextVisibleOnStart) { 1 } else { 0 })
            bundle.putInt(FirebaseAnalytics.Param.SCORE, (result as Result).items_correct)
            //if (task.type == Task.Type.TAP_SWIPE) { bundle.putInt(FirebaseAnalytics.Param.SCORE, (result as ResultGestures).items_correct) }
            //else { bundle.putInt(FirebaseAnalytics.Param.SCORE, (result as ResultTyping).words_correct) }
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
            view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.visibility = View.VISIBLE
            //do not want to call announce for accessibility here. Only set content description
            view?.findViewById<LinearLayout>(R.id.ll_main)?.contentDescription = getContentDescriptionForAccessibility()
        }


        val currentUser = FirebaseAuth.getInstance().currentUser
        mDatabase = FirebaseDatabase.getInstance().getReference("results")
        mDatabase?.keepSynced(true)
        val query = mDatabase?.orderByChild("userId")?.equalTo(currentUser!!.uid)
        //query?.addChildEventListener(mChildEventListener);

        val tv = view.findViewById<TextView>(R.id.tv_instruction)
        var instructions = (if (mTeachingContent is Task && (mTeachingContent as Task)?.durationMillis > 0) {
            String.format((mTeachingContent as Task)?.instructions + " " +
                    context?.resources?.getString(R.string.complete_as_many_as)
                    , (mTeachingContent as Task)?.getTimeLimitAsString(context))
        } else {
            (mTeachingContent as SEBaseObject)?.instructions
        }).toString()
        if (mTeachingContent is Task) {
            if ((mTeachingContent as Task).isExitOnInterruption) {
                instructions += context?.resources?.getString(R.string.activity_will_end_if_interrupted)
            }
        }
        (tv as TextView).text = instructions

        if (mTeachingContent is Course && mResults.isNotEmpty()) {
            tvProgress.visibility = View.VISIBLE
            tvProgress.text = Integer.toString(mResults.size) + "/" + (mTeachingContent as Course).tasks.size
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdListener()
        view.findViewById<LinearLayout>(R.id.ll_main).setOnTouchListener(this)
        setupScreenAccessibility()
    }

    fun setupScreenAccessibility() {
        val isTalkbackOn = (activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn

        if (isTalkbackOn) {
            view?.findViewById<TextView>(R.id.tv_single_tap_to_repeat)?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.tv_swipe_to_continue)?.text = context?.resources?.getString(R.string.swipe_with_2_fingers_continue_next)
            view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.text = context?.resources?.getString(R.string.tap_and_long_press_for_more_options)
            tvTapScreenToStart?.text = context?.resources?.getString(R.string.double_tap_screen_to_start)
            if (mTeachingContent is Course && mResults.isNotEmpty()) {
                tvTapScreenToStart?.text = context?.resources?.getString(R.string.double_tap_screen_to_continue)
            }
        }
        else {
            view?.findViewById<TextView>(R.id.tv_single_tap_to_repeat)?.visibility = View.GONE
            view?.findViewById<TextView>(R.id.tv_swipe_to_continue)?.text = context?.resources?.getString(R.string.swipe_continue_next)
            view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.text = context?.resources?.getString(R.string.long_press_for_more_options)
            tvTapScreenToStart?.text = context?.resources?.getString(R.string.tap_screen_to_start)
            if (mTeachingContent is Course && mResults.isNotEmpty()) {
                tvTapScreenToStart?.text = context?.resources?.getString(R.string.tap_screen_to_continue)
            }
        }

        var contentDescription = getContentDescriptionForAccessibility()
        view?.findViewById<LinearLayout>(R.id.ll_main)?.contentDescription = contentDescription
        view?.announceForAccessibility(contentDescription)
    }

    fun getContentDescriptionForAccessibility() : String {
        val isTalkbackOn = (activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn

        var contentDescription = view?.findViewById<TextView>(R.id.tv_instruction)?.text.toString()
        if (isTalkbackOn) {
            contentDescription += view?.findViewById<TextView>(R.id.tv_single_tap_to_repeat)?.text.toString()
        }

        contentDescription += " " + view?.findViewById<TextView>(R.id.tvTapScreenToStart)?.text.toString()
        if (view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.visibility == View.VISIBLE) {
            contentDescription += " " + view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.text.toString()
        }
        if (view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.visibility == View.VISIBLE) {
            contentDescription += " " + view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.text.toString()
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
        val type = Task.Type.fromInt(bundle.getLong("taskTypeLong"))
        val result =
                if (type == Task.Type.TYPING) {
                    firebase.writeNewResultTyping(
                            bundle.getInt("charactersCorrect")
                            ,bundle.getInt("totalCharactersAttempted")
                            ,bundle.getInt("wordsCorrect")
                            ,bundle.getInt("totalWordsFinished")
                            ,bundle.getLong("startTimeMillis")
                            ,bundle.getLong("timeTakenMillis")
                            ,bundle.getInt("taskId")
                            ,bundle.getInt("itemsAttempted")
                            ,bundle.getInt("itemsCorrect")
                            ,bundle.getParcelableArrayList("responses")
                    )
                } else {
                    firebase.writeNewResult(
                            bundle.getInt("itemsAttempted")
                            ,bundle.getInt("itemsCorrect")
                            ,bundle.getLong("startTimeMillis")
                            ,bundle.getLong("timeTakenMillis")
                            ,bundle.getInt("taskId")
                            ,bundle.getParcelableArrayList("responses")
                    )
                }
        //TEMPORARY MOVE AS TIMESTAMP IS ONLY CREATED ON THE SERVER
        result.timestamp = Calendar.getInstance().timeInMillis
        //////////
        analyticsTaskCompleted((mTeachingContent as Task), result)
        setReturnResult(result)
        mListener?.onTaskDetailFragmentShowLastTried(mTeachingContent, result, null, null)
        updateResults(result)

        val isTalkbackOn = (activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn
        if (mTeachingContent is Course && isTalkbackOn) {
            tvTapScreenToStart.text = context?.resources?.getString(R.string.double_tap_screen_to_continue)
        } else {
            tvTapScreenToStart.text = context?.resources?.getString(R.string.tap_screen_to_continue)
        }
        view?.findViewById<TextView>(R.id.tv_long_press_for_more_options)?.visibility = View.VISIBLE //If its a succesful result, set this to visible
        //do not want to call announce for accessibility here. Only set content description
        view?.findViewById<LinearLayout>(R.id.ll_main)?.contentDescription = getContentDescriptionForAccessibility()
    }

    private fun updateResults(result: Result) {
        if (result != null) {
            mResults?.add(result)
        }
    }

    private fun sendAnalytics(task: Task, view: View?, action: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Screen")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, task.type?.toString()?.replace("_", " "))
        bundle.putString("content_name", task.title)
        bundle.putInt("content_timed", if (task.timed) { 1 } else { 0 })
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
        fun newInstance(teachingContent: Parcelable?, results: ArrayList<Parcelable>): TaskDetailFragment {
            val fragment = TaskDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, teachingContent)
            args.putParcelableArrayList(ARG_RESULTS, results)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
