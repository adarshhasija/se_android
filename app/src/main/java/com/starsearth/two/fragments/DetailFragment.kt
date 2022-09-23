package com.starsearth.two.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.*

import com.starsearth.two.R
import com.starsearth.two.activity.MainActivity
import com.starsearth.two.application.StarsEarthApplication
import com.starsearth.two.managers.FirebaseManager
import com.starsearth.two.domain.*
import com.starsearth.two.listeners.SeOnTouchListener
import kotlinx.android.synthetic.main.fragment_detail.*
import java.util.*
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DetailFragment.OnDetailFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment(), SeOnTouchListener.OnSeTouchListenerInterface {

    override fun gestureTap() {
        if (tvTapScreenToStart.visibility == View.VISIBLE) {
            (activity?.application as? StarsEarthApplication)?.adsManager?.generateAd(mTeachingContent, mResults.toList())
            openTask()
        }
    }

    override fun gestureSwipe() {
        if (tvSwipeToContinue.visibility == View.VISIBLE) {
            if (mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                mListener?.onDetailFragmentSwipeInteraction(mTeachingContent)
            }
        }
    }

    override fun gestureLongPress() {
        if (tvLongPressForMoreOptions.visibility == View.VISIBLE) {
            mListener?.onDetailFragmentLongPressInteraction(mTeachingContent, mResults.toList())
        }
    }

    private lateinit var mContext: Context
    private var mTeachingContent: SETeachingContent? = null
    private var mResults: Queue<Result> = LinkedList() //Queue = So that we know which is first result and which is last result
    private var mFirstResult: Result? = null //This is used for animating the long press label

    private var mListener: OnDetailFragmentInteractionListener? = null
    private var mDatabase : DatabaseReference? = null

    /*
        If a previously passed task has been repeated. Result is irreleveant. Simple add it to the array
     */
    fun onTaskRepeated(result: Result) {
        addResultToQueue(result)
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

    private val mResultValuesListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val map = snapshot?.value
            if (map != null) {
                for (entry in (map as HashMap<*, *>).entries) {
                    val value = entry.value as Map<String, Any>
                    var newResult = Result(value)
                    if (mTeachingContent is Task) {
                        if ((mTeachingContent as Task)?.uid != newResult.task_id) {
                            //Only proceed if result belongs to this task
                            continue
                        }
                    }
                    if (mTeachingContent is Course) {
                        if (!(mTeachingContent as Course).isTaskExists(newResult.task_id.toString())) {
                            //Only proceed if result belongs to this course
                            continue
                        }
                    }
                    if ((mTeachingContent as? Course)?.getTaskById(newResult.task_id)?.type == Task.Type.SEE_AND_TYPE) {
                        newResult = ResultTyping(value)
                    }
                    addResultToQueue(newResult)

                }
                updateUI()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private val mTeachingContentListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val map = snapshot?.value
            if (map != null) {
                //TODO: Process TeachingContent here. Map contains values of a single object
                //var result = Result((map as Map<String, Any>))
            }
        }

        override fun onCancelled(error: DatabaseError) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTeachingContent = requireArguments().getParcelable(ARG_TEACHING_CONTENT)
         //   val parcelableArrayList : ArrayList<Parcelable>? = arguments?.getParcelableArrayList<Parcelable>(ARG_RESULTS)
         //   parcelableArrayList?.forEach { mResults.add((it as Result)) }
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("results")
        mDatabase?.keepSynced(true)
        (mContext as? MainActivity)?.mUser?.uid?.let {
            val query = mDatabase?.orderByChild("userId")?.equalTo(it)
            //query?.addChildEventListener(mChildEventListener);
            query?.addListenerForSingleValueEvent(mResultValuesListener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_detail, container, false)

        //Setup of adRequest was done here. Remove later if not needed

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clTask?.setOnTouchListener(SeOnTouchListener(this@DetailFragment))
        updateUI() //This must remain uncommented. UI should be visible even if no results are available

        if (mFirstResult != null) {
            mFirstResult = null
            //If its the first result, we animate the long press option to let the user know its there
            tvLongPressForMoreOptions?.let { animateTextViewLarge(it) }
        }
    }

    /*
        Call this listener if teaching content is on FirebaseManager and we need to call it
     */
    private fun setupTeachingContentListener(teachingContentUid: String?) {
        //mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("teaching_content")
        //mDatabaseResultsReference?.keepSynced(true)
        //val query = mDatabaseResultsReference?.child(teachingContentUid)
        //query?.addListenerForSingleValueEvent(mTeachingContentListener)
    }

    fun updateUI() {
        updateUIVisibility()
        updateUIText()
        clTask?.contentDescription = getContentDescriptionForAccessibility()
        view?.announceForAccessibility(clTask?.contentDescription)
    }

    fun updateUIVisibility() {
        //Set visibility for all UI
        tvInstruction?.visibility = View.VISIBLE
    /*    llTap?.visibility =
                if ((mTeachingContent as? Task)?.type == Task.Type.TAP_SWIPE) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }   */ //Sept 2022: Hiding this as true/false will now be handled using standard buttons. For Accessibility.
        llSlides?.visibility =
                if ((mTeachingContent as? Task)?.type == Task.Type.SLIDES) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        llTimeLimit?.visibility =
                if ((mTeachingContent as? Task)?.timed == true) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        llContentLength?.visibility =
                if ((mTeachingContent as? Task)?.ordered == true) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        llAudio?.visibility =
                if ((mTeachingContent as? Task)?.type == Task.Type.HEAR_AND_TYPE) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        llOrdered?.visibility =
                if ((mTeachingContent as? Task)?.ordered == true) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        llTyping?.visibility =
                if ((mTeachingContent as? Task)?.type == Task.Type.HEAR_AND_TYPE || (mTeachingContent as? Task)?.type == Task.Type.SEE_AND_TYPE) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
    /*    llSwipe?.visibility =
                if ((mTeachingContent as? Task)?.type == Task.Type.TAP_SWIPE) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }   */ //Sept 2022: Hiding this as true/false will now be handled using standard buttons. For Accessibility.
        tvProgress?.visibility =
                if (mTeachingContent is Course) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
        tvSingleTapToRepeat?.visibility = View.GONE
            /*    if ((activity?.application as StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }   */
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
        tvLongPressForMoreOptions?.visibility = View.GONE //Sept 2022: Not giving this right now  //View.VISIBLE //At minimum, user must always be able to see creator's name
            /*    if (mTeachingContent is Course
                        || (activity as? MainActivity)?.mEducator != null
                        || (mTeachingContent is Task && mResults.isNotEmpty())
                ) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }   */
    }

    fun updateUIText() {
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

        var instructions =
                if (mTeachingContent is Course && !(mTeachingContent as Course).isCourseComplete(mResults.toList())) {
                    //Get the instructions of next task
                    (mTeachingContent as Course).getNextTask(mResults.toList())?.instructions
                }
                else {
                    //Course or Task, get the normal instructions
                    (mTeachingContent as SETeachingContent)?.instructions
                }
    /*    if (mTeachingContent is Task) {
            if ((mTeachingContent as Task).durationMillis > 0) {
                instructions +=
                        context?.resources?.getString(R.string.complete_as_many_as) +
                        " " + (mTeachingContent as Task).getTimeLimitAsString(context)
            }
            if ((mTeachingContent as Task).isExitOnInterruption) {
                instructions += context?.resources?.getString(R.string.activity_will_end_if_interrupted)
            }
        }   */
        tvInstruction?.text = instructions

        tvContentLength?.text =
                if ((mTeachingContent as? Task)?.ordered == true) {
                    (mTeachingContent as? Task)?.content?.size.toString()
                }
                else {
                    mContext.getText(R.string.unknown).toString().toLowerCase().capitalize()
                }
        tvTimeLimit?.text =
                if ((mTeachingContent as? Task)?.durationMillis != null && (mTeachingContent as Task).durationMillis > 0) {
                    (((mTeachingContent as Task).durationMillis/1000)/60).toString() + " " + "minute"
                }
                else {
                    "0"
                }



        tvTapScreenToStart?.text = context?.resources?.getString(R.string.tap_screen_to_start) //Uncomment this if user auth is not being used
            /*    if ((context as? MainActivity)?.mUser == null && (mTeachingContent as? Task)?.type != Task.Type.SLIDES) {
                    context?.resources?.getString(R.string.tap_screen_to_login)
                }
                else if ((activity?.application as StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                    //Assuming the user is logged in
                    context?.resources?.getString(R.string.double_tap_or_enter_to_start)
                }
                else {
                    context?.resources?.getString(R.string.tap_screen_to_start)
                }   */ //Uncomment this if user authentication is being used and we need to notify user to login

        tvSwipeToContinue?.text = ""
             /*   if ((activity?.application as StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true && mTeachingContent is Course && (mTeachingContent as Course).hasKeyboardTest) {
                    context?.resources?.getString(R.string.swipe_with_2_fingers_for_keyboard_test)
                }
                else {
                    context?.resources?.getString(R.string.swipe_for_keyboard_test)
                }   */

        tvLongPressForMoreOptions?.text = context?.resources?.getString(R.string.long_press_for_more_options)
            /*    if ((activity?.application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                    context?.resources?.getString(R.string.tap_and_long_press_for_more_options)
                }
                else {
                    context?.resources?.getString(R.string.long_press_for_more_options)
                }   */

        clTask?.contentDescription = getContentDescriptionForAccessibility() //set for accessibility
    }

    /*
        If a particular textview needs to be shown large in size and animated down in order to draw attention to it
     */
    private fun animateTextViewLarge(textView: TextView) {
        val startSize = 18f // Size in pixels
        val endSize = 30f
        val animationDuration : Long = 2000; // Animation duration in ms


        val animator = ValueAnimator.ofFloat(endSize, startSize)
        animator.setDuration(animationDuration)

        animator.addUpdateListener {
            val animatedValue : Float = it.animatedValue as Float
            textView.textSize = animatedValue
        }
        animator.start()
    }

    fun getContentDescriptionForAccessibility() : String {
        val isTalkbackOn = (activity?.application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn

        var contentDescription = tvInstruction?.text.toString()
        if (isTalkbackOn == true) {
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

    fun onEnterTapped() {
        openTask()
    }

    private fun openTask() {
        var task : Task? =
                if (mTeachingContent is Course) {
                    (mTeachingContent as? Course)?.getNextTask(mResults.toList())
                }
                else {
                    mTeachingContent as Task
                }

        task?.let {
            mListener?.onDetailFragmentTapInteraction(task)
        }
    }

    /*
    onActivityResult Helpers
     */
    fun onActivityResultCancelled(data: Intent?) {
        val extras = data?.extras
        val reason = extras?.get(Task.FAIL_REASON)
        if (reason == Task.NO_ATTEMPT) {
            mListener?.onDetailFragmentShowMessage(getString(R.string.cancelled), getString(R.string.no_attempt))
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
            mListener?.onDetailFragmentShowMessage(getString(R.string.cancelled), getString(R.string.typing_game_cancelled))
        }
    }

    private fun addResultToQueue(result: Result) {
        if (mResults.size == 0) {
            mResults.add(result)
        }
        else if (mResults.size > 0 && mResults.last().uid != result.uid) {
            //We must check that this result is not a duplicate of the last element
            mResults.add(result)
        }
    }

    //If a user has entered the app through a public channel without logging in, eg: search option on the login screen
    //It means they can reach any task without login. If login is required, MainActivity will call this function at the
    //end of the flow to confirm to the fragment that login is complete
    fun onLoginComplete(uid : String) {
        if (uid != null) {
            tvTapScreenToStart?.text = context?.resources?.getString(R.string.tap_screen_to_start)
               /*     if ((activity?.application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                        //Assuming the user is logged in
                        context?.resources?.getString(R.string.double_tap_or_enter_to_start)
                    }
                    else {
                        context?.resources?.getString(R.string.tap_screen_to_start)
                    }   */

            tvTapScreenToStart?.visibility = View.VISIBLE
            tvTapScreenToStart?.let { animateTextViewLarge(it) }

            mDatabase = FirebaseDatabase.getInstance().getReference("results")
            mDatabase?.keepSynced(true)
            val query = mDatabase?.orderByChild("userId")?.equalTo(uid)
            //query?.addChildEventListener(mChildEventListener);
            query?.addListenerForSingleValueEvent(mResultValuesListener)
        }
    }

    fun onActivityResultOK(data: Intent?) {
        data?.extras?.let {
            taskComplete(it)
        }
        if (mResults.size > 0) {
            if ((activity?.application as? StarsEarthApplication)?.adsManager?.shouldShowAd(mTeachingContent, mResults.last()) == true) {
                (activity?.application as? StarsEarthApplication)?.adsManager?.showAd()
            }
        }

    }

    private fun taskComplete(bundle: Bundle) {
        val firebase = FirebaseManager("results")
        val resultMap : HashMap<String, Any> = bundle.getSerializable("result_map") as HashMap<String, Any>
        val type = Task.Type.fromInt((resultMap["taskTypeLong"] as Long)) //Task.Type.fromInt(bundle.getLong("taskTypeLong"))
        val result =
                if (type == Task.Type.SEE_AND_TYPE) {
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

        //2. Update previous fragments with new result
        mListener?.onDetailFragmentTaskCompleted(result)

        //3. Update the Fragment's mResults array
        addResultToQueue(result)

        //4. If the task is passed and we have reached the end of the course, push end of course message
        if (mTeachingContent is Course && (mTeachingContent as Course).isCourseComplete(mResults.toList())) {
            mListener?.onDetailFragmentShowMessage(getString(R.string.congratulations), getString(R.string.course_complete))
        }

        //5. If the task is passed and a checkpoint has been reached, push checkpoint fragment next
        if (mTeachingContent is Course &&
                (mTeachingContent as Course).getTaskById(result.task_id).isPassed(result) &&
                            (mTeachingContent as Course).checkpoints.containsKey(result.task_id))
        {
            mListener?.onDetailFragmentShowMessage(getString(R.string.checkpoint_reached), ((mTeachingContent as Course).checkpoints.get(result.task_id) as Checkpoint).title)
        }

        //6. Update UI
        updateUI()

        //7. Show the results screen to the user
        mListener?.onDetailFragmentShowLastTried(mTeachingContent, result)

        //8. If this is the first time the task was attempted, set mFirstResult. Has to be set here as addResultToQueue is called from multiple places
        if (mResults.size == 1) {
            mFirstResult = result
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDetailFragmentInteractionListener) {
            mListener = context
            mContext = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnDetailFragmentInteractionListener")
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
    interface OnDetailFragmentInteractionListener {
        fun onDetailFragmentTapInteraction(task: Task)
        fun onDetailFragmentSwipeInteraction(teachingContent: Any?)
        fun onDetailFragmentLongPressInteraction(teachingContent: Any?, results: List<Result>)
        fun onDetailFragmentShowLastTried(teachingContent: Any?, result: Any?)
        fun onDetailFragmentShowMessage(title: String?, message: String?)
        fun onDetailFragmentTaskCompleted(result: Result)
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        val TAG = "DetailFragment"
        private val ARG_TEACHING_CONTENT = "TEACHING_CONTENT"


        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(teachingContent: Parcelable?): DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, teachingContent)
            //args.putParcelableArrayList(ARG_RESULTS, results)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
