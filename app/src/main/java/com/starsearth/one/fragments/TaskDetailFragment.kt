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
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.starsearth.one.R
import com.starsearth.one.activity.tasks.TaskActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.database.Firebase
import com.starsearth.one.domain.*
import com.starsearth.one.managers.AdsManager
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
                    gestureSwipe()
                } else if (Math.abs(actionUpTimestamp - actionDownTimestamp) > 500) {
                    gestureLongPress()
                } else {
                    gestureTap()
                }
            }
        }
        return true
    }



    private fun gestureTap() {
        if (tvTapScreenToStart.visibility == View.VISIBLE) {
            generateAd()
            var task : Task? =
                    if (mTeachingContent is Course) {
                        (mTeachingContent as? Course)?.getNextTask(mResults?.toList())
                    }
                    else {
                        mTeachingContent as Task
                    }

            task?.let {
                mListener?.onTaskDetailFragmentTapInteraction(task)
            }
        }

    }

    private fun gestureLongPress() {
        if (tvLongPressForMoreOptions.visibility == View.VISIBLE) {
            mListener?.onTaskDetailFragmentLongPressInteraction(mTeachingContent, mResults.toList())
        }
    }

    private fun gestureSwipe() {
        if (tvSwipeToContinue.visibility == View.VISIBLE) {
            if (mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                mListener?.onTaskDetailFragmentSwipeInteraction(mTeachingContent)
            }
        }
    }

    // TODO: Rename and change types of parameters
    private var mTeachingContent: Any? = null
    private var mResults: Queue<Result> = LinkedList() //Queue = So that we know which is first result and which is last result
    private var mReturnBundle = Bundle()

    private var mListener: OnTaskDetailFragmentInteractionListener? = null
    private var adRequest: AdRequest.Builder? = null
    private var mDatabase : DatabaseReference? = null

    /*
    Call this only after at least 1 result is succesfully received in onActivityResult
     */
    fun shouldShowAd() : Boolean {
        val isTeachingContentAllowingAd =
            if (mTeachingContent is Course) {
                //If its a Course, only show if the task was passed
                //mResults size should never be 0 here. It is called after saving a Result in onActivityResult
                (mTeachingContent as Course).shouldShowAd(mResults.last())
            }
            else {
                //If its a Task, no additional validation required
                true
            }
        return isTeachingContentAllowingAd
    }

    fun showAd() {
        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().ads
        if (ads == "Google" &&
                (activity?.application as StarsEarthApplication)?.googleInterstitialAd?.isLoaded == true) {
            (activity?.application as StarsEarthApplication)?.googleInterstitialAd.show()
        }
        else if (ads == "Facebook" &&
                (activity?.application as StarsEarthApplication)?.facebookInterstitalAd?.isAdLoaded == true) {
            (activity?.application as StarsEarthApplication)?.facebookInterstitalAd.show()
        }
    }

    fun generateAd() {
        val ads = (activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().ads

        if (AdsManager.shouldGenerateAd(context?.applicationContext, mTeachingContent, mResults.toList())) {
            if (ads == "Google" && adRequest != null) {
                (activity?.application as StarsEarthApplication)?.googleInterstitialAd.loadAd(adRequest?.build())
            }
            else if (ads == "Facebook") {
                //AdSettings.addTestDevice("cc5a9eab-c86b-4529-83bb-902568670129"); //TS Mac simulator
                //AdSettings.addTestDevice("171f080c-a50d-457c-9226-bcdc194fda20"); //AH Mac simulator
                (activity?.application as StarsEarthApplication)?.facebookInterstitalAd.loadAd()
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
                dataSnapshot?.getValueString(ResultTyping::class.java)
            } else {
                dataSnapshot?.getValueString(Result::class.java)
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
        bundle.putInt("type", task.getType().getValue().toInt())
        bundle.putBoolean("timed", task.timed)
        bundle.putBoolean("isGame", task.isGame)
        bundle.putInt(FirebaseAnalytics.Param.SCORE, (result as Result).items_correct)

        val score = bundle?.getInt(FirebaseAnalytics.Param.SCORE)
        (activity?.application as StarsEarthApplication)?.analyticsManager?.logActionEvent("se1_post_score", bundle, score)
    }

    private val mResultsMultipleValuesListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val map = dataSnapshot?.value
            if (map != null) {
                for (entry in (map as HashMap<*, *>).entries) {
                    val value = entry.value as Map<String, Any>
                    var newResult = Result(value)
                    if ((mTeachingContent as SEBaseObject)?.id != newResult!!.task_id) {
                        //Only proceed if result belongs to this teaching content
                        continue
                    }
                    if ((mTeachingContent as? Course)?.getTaskById(newResult.task_id)?.type == Task.Type.TYPING) {
                        newResult = ResultTyping(value)
                    }
                    mResults.add(newResult)
                }
                updateUI()
            }
        }

        override fun onCancelled(p0: DatabaseError?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTeachingContent = arguments?.getParcelable(ARG_TEACHING_CONTENT)
         //   val parcelableArrayList : ArrayList<Parcelable>? = arguments?.getParcelableArrayList<Parcelable>(ARG_RESULTS)
         //   parcelableArrayList?.forEach { mResults.add((it as Result)) }
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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        mDatabase = FirebaseDatabase.getInstance().getReference("results")
        mDatabase?.keepSynced(true)
        val query = mDatabase?.orderByChild("userId")?.equalTo(currentUser!!.uid)
        //query?.addChildEventListener(mChildEventListener);
        query?.addListenerForSingleValueEvent(mResultsMultipleValuesListener)

        clTask?.setOnTouchListener(this)
        //updateUI()
    }

    fun updateUI() {
        updateUIVisibility()
        updateUIText()
        clTask?.contentDescription = getContentDescriptionForAccessibility()
        view?.announceForAccessibility(clTask?.contentDescription)
    }

    fun updateUIVisibility() {
        //Set visibility for all UI
        tvCourseDescription?.visibility =
                if (mTeachingContent is Course) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvInstruction?.visibility = View.VISIBLE
        tvProgress?.visibility =
                if (mTeachingContent is Course) {
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
                if (mTeachingContent is Course && (mTeachingContent as Course).isCourseComplete(mResults.toList())) {
                    View.GONE
                }
                else {
                    View.VISIBLE
                }
        tvSwipeToContinue?.visibility = View.GONE
             /*   if (mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }   */
        tvLongPressForMoreOptions?.visibility =
                if (mTeachingContent is Course || mTeachingContent is Task && mResults.isNotEmpty()) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
    }

    fun updateUIText() {
        tvCourseDescription?.text =
                if (mTeachingContent is Course) {
                    (mTeachingContent as Course).description
                }
                else {
                    ""
                }
        tvProgress?.text =
                if (mTeachingContent is Course && (mTeachingContent as Course).isCourseComplete(mResults.toList())) {
                    context?.resources?.getText(R.string.complete)
                }
                else if (mTeachingContent is Course && (mTeachingContent as Course).isFirstTaskPassed(mResults.toList())){
                    val nextTaskNumber = (mTeachingContent as Course).getNextTaskIndex(mResults.toList()) + 1
                    context?.resources?.getString(R.string.next_task) + "\n" + nextTaskNumber + "/" + (mTeachingContent as Course).tasks.size
                }
                else if (mTeachingContent is Course) {
                    //If the course has not started
                    context?.resources?.getString(R.string.first_task) + "\n" + "1" + "/" + (mTeachingContent as Course).tasks.size
                } else {
                    //If the teaching content item is a Task
                    ""
                }

        val tv = tvInstruction
        var instructions =
                if (mTeachingContent is Course && !(mTeachingContent as Course).isCourseComplete(mResults.toList())) {
                    //Get the instructions of next task
                    (mTeachingContent as Course).getNextTask(mResults.toList())?.instructions
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

        tvSwipeToContinue?.text = ""
             /*   if ((activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn && mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                    context?.resources?.getString(R.string.swipe_with_2_fingers_for_keyboard_test)
                }
                else {
                    context?.resources?.getString(R.string.swipe_for_keyboard_test)
                }   */

        tvLongPressForMoreOptions?.text =
                if ((activity?.application as StarsEarthApplication)?.accessibility.isTalkbackOn) {
                    context?.resources?.getString(R.string.tap_and_long_press_for_more_options)
                }
                else {
                    context?.resources?.getString(R.string.long_press_for_more_options)
                }

        clTask?.contentDescription = getContentDescriptionForAccessibility() //set for accessibility
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

    /*
    onActivityResult Helpers
     */
    fun onActivityResultCancelled(data: Intent?) {
        val extras = data?.extras
        val reason = extras?.get(Task.FAIL_REASON)
        if (reason == Task.NO_ATTEMPT) {
            mListener?.onTaskDetailFragmentShowMessage(getString(R.string.cancelled), getString(R.string.no_attempt))
        }
        else if (reason == Task.GESTURE_SPAM) {
            val alertDialog = (activity?.application as StarsEarthApplication).createAlertDialog(context)
            alertDialog.setTitle(getString(R.string.gesture_spam_detected))
            alertDialog.setMessage((activity?.application as StarsEarthApplication).getFirebaseRemoteConfigWrapper().gestureSpamMessage)
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton(getString(android.R.string.ok), null)
            alertDialog.show()
        }
        else {
            //Show cancelled message
            mListener?.onTaskDetailFragmentShowMessage(getString(R.string.cancelled), getString(R.string.typing_game_cancelled))
        }
    }

    fun onActivityResultOK(data: Intent?) {
        data?.let {
            taskComplete(it.extras)
        }
        if (shouldShowAd()) {
            showAd()
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

        //4. If the task is passed and we have reached the end of the course, push end of course message
        if (mTeachingContent is Course && (mTeachingContent as Course).isCourseComplete(mResults.toList())) {
            mListener?.onTaskDetailFragmentShowMessage(getString(R.string.congratulations), getString(R.string.course_complete))
        }

        //5. If the task is passed and a checkpoint has been reached, push checkpoint fragment next
        if (mTeachingContent is Course &&
                (mTeachingContent as Course).getTaskById(result.task_id).isPassed(result) &&
                            (mTeachingContent as Course).checkpoints.containsKey(result.task_id))
        {
            mListener?.onTaskDetailFragmentShowMessage(getString(R.string.checkpoint_reached), ((mTeachingContent as Course).checkpoints.get(result.task_id) as Checkpoint).title)
        }

        //6. Update UI
        updateUI()

        //7. Show the results screen to the user
        mListener?.onTaskDetailFragmentShowLastTried(mTeachingContent, result)
    }

    override fun onResume() {
        super.onResume()
        (activity?.application as StarsEarthApplication)?.logFragmentViewEvent(this.javaClass.simpleName, activity!!)
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
        fun onTaskDetailFragmentTapInteraction(task: Task)
        fun onTaskDetailFragmentSwipeInteraction(teachingContent: Any?)
        fun onTaskDetailFragmentLongPressInteraction(teachingContent: Any?, results: List<Result>)
        fun onTaskDetailFragmentShowLastTried(teachingContent: Any?, result: Any?)
        fun onTaskDetailFragmentShowMessage(title: String?, message: String?)
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        val FRAGMENT_TAG = "DetailFragment"
        private val ARG_TEACHING_CONTENT = "TEACHING_CONTENT"


        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TaskDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(teachingContent: Parcelable?/*, results: ArrayList<Parcelable>? */): TaskDetailFragment {
            val fragment = TaskDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, teachingContent)
            //args.putParcelableArrayList(ARG_RESULTS, results)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
