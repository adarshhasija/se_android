package com.starsearth.one.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdSettings
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.starsearth.one.R
import com.starsearth.one.Utils
import com.starsearth.one.activity.tasks.TaskActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ResultFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : Fragment(), View.OnTouchListener {

    private var x1: Float = 0.toFloat()
    private var x2:Float = 0.toFloat()
    private var y1:Float = 0.toFloat()
    private var y2:Float = 0.toFloat()
    internal val MIN_DISTANCE = 150
    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        when (event?.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event?.getX()
                y1 = event?.getY()
            }
            MotionEvent.ACTION_UP -> {
                x2 = event?.getX()
                y2 = event?.getY()
                val deltaX = x2 - x1
                val deltaY = y2 - y1
                if (Math.abs(deltaX) > MIN_DISTANCE || Math.abs(deltaY) > MIN_DISTANCE) {
                    gestureSwipe(p0)
                } else {
                    gestureTap(p0)
                }
            }
        }
        return true
    }

    private fun gestureTap(view: View?) {
        listFragment?.clearJustCompleteResultsSet()
        generateAd()
        startTask((mTeachingContent as Task))
        sendAnalytics((mTeachingContent as Task), view)
    }

    private fun gestureSwipe(view: View?) {
        mListener?.onResultFragmentSwipeInteraction(mTeachingContent)
    }

    // TODO: Rename and change types of parameters
    private var mTeachingContent: Any? = null
    private var mResults: Stack<Result> = Stack()

    private var mListener: OnFragmentInteractionListener? = null

    private var adRequest: AdRequest.Builder? = null

    private var listFragment : ResultListFragment? = null

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
        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().get("ads")
        if (ads == "Google") {
            (activity?.application as StarsEarthApplication)?.googleInterstitialAd.show()
        }
        else if (ads == "Facebook") {
            (activity?.application as StarsEarthApplication)?.facebookInterstitalAd.show()
        }
    }

    fun setupAdListener() {
        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().get("ads")
        if (ads == "Google") {
            (activity?.application as StarsEarthApplication)?.googleInterstitialAd.adListener = mGoogleAdListener
        }
        else if (ads == "Facebook") {
            (activity?.application as StarsEarthApplication)?.facebookInterstitalAd.setAdListener(mFacebookAdListener)
        }
    }

    fun generateAd() {
        val accessibilityUser = (activity?.application as StarsEarthApplication).accessibility.isAccessibilityUser
        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().get("ads")
        //only generate ads for non-accessibility users
        if (ads != "None" && !accessibilityUser) {
            val moduloString = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().get("ads_frequency_modulo")
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
                    AdSettings.addTestDevice("b8441b0c-b48d-4d5e-8d36-c67770d5bf01"); //TS Mac simulator
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
            val result = if ((mTeachingContent as Task)?.type == Task.Type.TYPING) {
                dataSnapshot?.getValue(ResultTyping::class.java)
            } else {
                dataSnapshot?.getValue(ResultGestures::class.java)
            }
            if ((mTeachingContent as SEBaseObject)?.id != result!!.task_id) {
                return;
            }
            setReturnResult(result)
            view?.findViewById<TextView>(R.id.tv_swipe_for_more_options)?.visibility = View.VISIBLE

            if (mResults.empty() || !isResultExistsInStack(result)) {
                mResults.push(result)
                //evaluateList((mResults as MutableList<Result>))
                if (result.isJustCompleted) {
                    mListener?.onResultFragmentShowLastTried(mTeachingContent, result, null, null)
                }
            }

            //setLastTriedUI(mTeachingContent, result)
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
        }
    }

    /**************************/

    private fun isResultExistsInStack(result: Result?) : Boolean {
        var ret = false
        if (!mResults.empty()) {
            if (mResults.peek().uid == result?.uid) {
                ret = true
            }
        }
        return ret
    }

    private fun setReturnResult(result: Any?) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putString("uid", (result as Result)?.uid)
        intent.putExtras(bundle)
        activity?.setResult(Activity.RESULT_OK, intent)
    }


    fun analyticsTaskCompleted(eventName: String, bundle: Bundle) {
        val application = (activity?.application as StarsEarthApplication)
        val score = bundle?.getInt(FirebaseAnalytics.Param.SCORE)
        application.logActionEvent(eventName, bundle, score)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTeachingContent = arguments?.getParcelable(ARG_TEACHING_CONTENT)
        }
    }

    /*
    If list size > 2, remove items that are not last_tired and not highscore. NOT WORKING AS EXPECTED
     */
    fun evaluateList(list: MutableList<Result>) {
        var lowestScoreIndex: Int = 0
        var lowestScore: Int = -1
        var lowestScoreId: String? = null

        if (list.size > 2) {
            //last item in the array is last_tried
            //do not want to iterate till that
            for (i in 0 until list.size-1) {
                val score = if (list[i] is ResultTyping) {
                    (list[i] as ResultTyping).words_correct
                } else if (list[i] is ResultGestures) {
                    (list[i] as ResultGestures).items_correct
                } else {
                    0
                }
                if (lowestScore == -1 || score < lowestScore) {
                    lowestScore = score
                    lowestScoreIndex = i
                    lowestScoreId = list[i].uid
                }
            }
            list.removeAt(lowestScoreIndex)
            mDatabase?.child(lowestScoreId)?.removeValue()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_result, container, false)

        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().get("ads")
        if (ads == "Google") {
            adRequest = AdRequest.Builder()
            if (mTeachingContent is Task) {
                val tags = (mTeachingContent as Task).tags
                for (tag in tags) {
                    adRequest?.addKeyword(tag)
                }
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        mDatabase = FirebaseDatabase.getInstance().getReference("results")
        mDatabase?.keepSynced(true)
        val query = mDatabase?.orderByChild("userId")?.equalTo(currentUser!!.uid)
        query?.addChildEventListener(mChildEventListener);


        v.findViewById<Button>(R.id.btn_start).setOnClickListener(View.OnClickListener {
            //onButtonPressed(mTeachingContent)
          /*  listFragment?.clearJustCompleteResultsSet()
            generateAd()
            startTask((mTeachingContent as Task))
            sendAnalytics((mTeachingContent as Task), it)   */
        })
        val tv = v.findViewById<TextView>(R.id.tv_instruction)

        (tv as TextView).text =
                (if (mTeachingContent is Task && (mTeachingContent as Task)?.durationMillis > 0) {
                    String.format((mTeachingContent as Task)?.instructions + " " +
                            context?.resources?.getString(R.string.complete_as_many_as)
                            //+ " " + appendScoreType()
                            , (mTeachingContent as Task)?.getTimeLimitAsString(context)) +
                            "\n\n" //+ appendCTAText()
                } else if (mTeachingContent is Task) {
                    String.format((mTeachingContent as Task)?.instructions + " " +
                            context?.resources?.getString(R.string.do_this_number_times) + " " +
                            //appendScoreType() + " " +
                            context?.resources?.getString(R.string.target_accuracy), (mTeachingContent as Task)?.trials) +
                            "\n\n" +
                            appendCTAText()
                } else if (mTeachingContent is Course){
                    (mTeachingContent as Course).instructions + " " +
                            "\n\n" //+ appendCTAText()
                } else {
                    ""
                }).toString()


        listFragment = ResultListFragment.newInstance((mTeachingContent as Parcelable))
        val transaction = childFragmentManager.beginTransaction()
        //transaction.add(R.id.fragment_container_list, listFragment).commit()


        return v
    }

    private fun setLastTriedUI(teachingContent: Any?, result: Any?) {
        if (teachingContent is Task) {
            view?.findViewById<TextView>(R.id.tv_result)?.text =
                    if (result is ResultTyping) {
                        result.getScoreSummary(context, teachingContent.timed)
                    } else if (result is ResultGestures) {
                        result.getScoreSummary(context, teachingContent.type)
                    } else {
                        ""
                    }
        }

        view?.findViewById<TextView>(R.id.tv_last_tried)?.text = Utils.formatDateTime((result as Result).timestamp)
        view?.findViewById<ConstraintLayout>(R.id.layout_last_tried)?.visibility = View.VISIBLE
    }

    private fun appendScoreType() : String {
        return "" + context?.resources?.getString(R.string.your_most_recent_score)
    }

    private fun appendCTAText() : String {
        return context?.resources?.getString(R.string.tap_screen_to_start) +
                "\n" +
                context?.resources?.getString(R.string.swipe_for_more_options)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdListener()
        view.findViewById<LinearLayout>(R.id.ll_main).setOnTouchListener(this)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            val extras = data?.extras
            val reason = extras?.get("reason")
            if (reason != null) {
                if (reason == "no attempt") {
                    //Toast.makeText(context, R.string.cancelled_no_attempt, Toast.LENGTH_LONG).show()
                    mListener?.onResultFragmentShowLastTried(null, null, getString(R.string.cancelled), getString(R.string.no_attempt))
                }
                else if (reason == "gesture spam") {
                    val message = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().get("gesture_spam_message")

                    val alertDialog = (activity?.application as StarsEarthApplication).createAlertDialog(context)
                    alertDialog.setTitle(getString(R.string.gesture_spam_detected))
                    alertDialog.setMessage((activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().get("gesture_spam_message"))
                    alertDialog.setNeutralButton(getString(android.R.string.ok), null)
                    //alertDialog.show()

                    mListener?.onResultFragmentShowLastTried(null, null, getString(R.string.gesture_spam_detected), message)
                }
            }
            else {
                //Toast.makeText(context, R.string.typing_game_cancelled, Toast.LENGTH_LONG).show()
                mListener?.onResultFragmentShowLastTried(null, null, getString(R.string.cancelled), getString(R.string.typing_game_cancelled))
            }

        }
        else if (requestCode == 0 && isAdAvailable == true) {
            showAd()
        }
    }

    private fun sendAnalytics(task: Task, view: View?) {
        val bundle = Bundle()
        //bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, task.id)
        //bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.title)
        //bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, task.type?.toString()?.replace("_", " "))
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Screen")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, task.type?.toString()?.replace("_", " "))
        bundle.putString("content_name", task.title)
        bundle.putInt("content_timed", if (task.timed) { 1 } else { 0 })
        val application = (activity?.application as StarsEarthApplication)
        application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
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
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun flashHighScore() {
        val mContentView = view?.findViewById<TextView>(R.id.tv_high_score) as TextView
        mContentView.alpha = 0f
        mContentView.visibility = View.VISIBLE

        mContentView.animate()
                .alpha(1f)
                .setDuration(150)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mContentView.visibility = View.GONE
                    }
                })
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
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onResultFragmentSwipeInteraction(teachingContent: Any?)
        fun onResultFragmentShowLastTried(teachingContent: Any?, result: Any?, title: String?, message: String?)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_COURSE = "course"
        private val ARG_TEACHING_CONTENT = "TEACHING_CONTENT"
        private val ARG_RESULTS = "RESULTS"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param0: Parcelable?): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, param0)
            //args.putParcelableArray(ARG_RESULTS, (param1?.toArray() as Array<out Parcelable>))
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
