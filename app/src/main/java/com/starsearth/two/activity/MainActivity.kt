package com.starsearth.two.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import com.starsearth.two.R
import com.starsearth.two.activity.auth.AddEditPhoneNumberActivity
import com.starsearth.two.activity.profile.PhoneNumberActivity
import com.starsearth.two.application.StarsEarthApplication
import com.starsearth.two.domain.*
import com.starsearth.two.fragments.*
import com.starsearth.two.fragments.dummy.DummyContent
import com.starsearth.two.fragments.lists.*
import com.starsearth.two.managers.AnalyticsManager
import com.starsearth.two.managers.FirebaseManager


class MainActivity : AppCompatActivity(),
        RecordListFragment.OnRecordListFragmentInteractionListener,
        DetailFragment.OnDetailFragmentInteractionListener,
        DetailListFragment.OnTaskDetailListFragmentListener,
        ResultListFragment.OnResultListFragmentInteractionListener,
        CourseProgressListFragment.OnCourseProgressListFragmentInteractionListener,
        ResultDetailFragment.OnResultDetailFragmentInteractionListener,
        ResponseListFragment.OnResponseListFragmentInteractionListener,
        CourseDescriptionFragment.OnFragmentInteractionListener,
        AnswerExplanationFragment.OnFragmentInteractionListener,
        AutismStoryFragment.OnListFragmentInteractionListener,
        ProfileEducatorFragment.OnProfileEducatorFragmentInteractionListener,
        TagListFragment.OnListFragmentInteractionListener,
        ProfileEducatorPermissionsListFragment.OnListFragmentInteractionListener,
        EducatorContentFragment.OnListFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        SearchResultItemFragment.OnListFragmentInteractionListener,
        SeOneListFragment.OnSeOneListFragmentInteractionListener {

    override fun onSearchResultListFragmentInteraction(selectedItem: Parcelable, type: String?) {
        val recordsListFragment = RecordListFragment.newInstance(selectedItem, type)
        openFragment(recordsListFragment, RecordListFragment.TAG)
    }

    override fun onSearchResultsObtained(resultsList: java.util.ArrayList<Parcelable>, type: String) {
        val fragment = SearchResultItemFragment.newInstance(resultsList, type)
        openFragmentWithSlideToLeftEffect(fragment, SearchResultItemFragment.TAG)
    }

    override fun onEducatorContentListFragmentInteraction(teachingContent: Parcelable) {

    }

    override fun onProfileEducatorPermissionsListFragmentInteraction() {

    }

    override fun onTagsSaveCompleted() {
        supportFragmentManager?.popBackStackImmediate()
        val alertDialog = (application as? StarsEarthApplication)?.createAlertDialog(this)
        alertDialog?.setTitle(getString(R.string.saved))
        alertDialog?.setMessage(getString(R.string.save_successful))
        alertDialog?.setPositiveButton(getString(android.R.string.ok), null)
        alertDialog?.show()
    }

    override fun onProfilePicTapped(imgByteArray: ByteArray) {
        val intent = Intent(this@MainActivity, FullScreenActivity::class.java)
        val bundle = Bundle()
        bundle.putByteArray(FullScreenActivity.IMG_BYTE_ARRAY, imgByteArray)
        bundle.putString(FullScreenActivity.VIEW_TYPE, FullScreenActivity.VIEW_TYPE_PROFILE_PIC)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onViewContentBtnTapped() {
        val fragment = EducatorContentFragment.newInstance()
        openFragment(fragment, EducatorContentFragment.TAG)
    }

    override fun onViewPermissionsBtnTapped(educator: Educator) {
        val fragment = ProfileEducatorPermissionsListFragment.newInstance(educator)
        openFragment(fragment, ProfileEducatorPermissionsListFragment.TAG)
    }

    override fun onProfileEducatorCTATapped(parentItemSelected: DetailListFragment.ListItem, educator: Educator, teacingContent: SETeachingContent) {
        supportFragmentManager.popBackStackImmediate()
        when (parentItemSelected) {
            DetailListFragment.ListItem.CHANGE_TAGS -> {
                val fragment = TagListFragment.newInstance(teacingContent)
                openFragmentWithSlideToLeftEffect(fragment, TagListFragment.TAG)
            }
            else -> {

            }
        }
    }


    override fun onProfileEducatorStatusChanged() {
        updatedEducatorProperties()
    }


    override fun onAnswerExplanationFragmentInteraction() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCourseDescriptionFragmentInteraction(course: Course) {
        //Get rid of CourseDescriptionFragment
        supportFragmentManager?.popBackStackImmediate()

        val fragment = DetailFragment.newInstance(course as Parcelable)
        openFragmentWithSlideToLeftEffect(fragment, DetailFragment.TAG)

    }

    override fun onDetailFragmentTaskCompleted(result: Result) {
        //We should loop through all the fragments as we could have multiple instances of each fragment
        //Once a fragment type is complete we should not process it again if we find it later in the array
        var recordListFragmentComplete = false
        var detailFragmentComplete = false
        val backStackCount = supportFragmentManager.backStackEntryCount
        for (index in 0 until backStackCount) {
            val backEntry = supportFragmentManager.getBackStackEntryAt(index)
            if (backEntry.name == RecordListFragment.TAG && !recordListFragmentComplete && index < backStackCount - 1) {
                //Once a task has been completed, add it to the record list fragment
                //Once we return to that screen, we will simply update the list
                //Should only do this on the first instance of RecordListFragment. There could be another instance later for viewing completed tasks
                //Should only do it if this instance is not the current active instance. ie: Not the last item on the stack
                val fragment = supportFragmentManager?.findFragmentByTag(RecordListFragment.TAG)
                (fragment as? RecordListFragment)?.taskCompleted(result)
                recordListFragmentComplete = true
            }
            else if (backEntry.name == DetailFragment.TAG && !detailFragmentComplete && index < backStackCount - 1) {
                //If we have just repeated a task thats part of a course(by tapping REPEAT_PREVIOUSLY_PASSED_TASKS)
                //Update the detail fragment with the result.
                //This should only be done for the first instance of DetailFragment
                //Should only do it if this instance is not the current active instance. ie: Not the last item on the stack
                val fragment = supportFragmentManager?.findFragmentByTag(DetailFragment.TAG)
                (fragment as? DetailFragment)?.onTaskRepeated(result)
                detailFragmentComplete = true
            }
        }
    }

    override fun onDetailListItemLongPress(itemTitle: DetailListFragment.ListItem, teachingContent: SETeachingContent?, results: ArrayList<Result>) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailListItemLongPress(itemTitle.toString(), teachingContent)
        when (itemTitle) {
            DetailListFragment.ListItem.HIGH_SCORE -> {
                val intent = Intent(this@MainActivity, FullScreenActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable(FullScreenActivity.TASK, (teachingContent as Task))
                bundle.putParcelable(FullScreenActivity.RESULT, teachingContent.getHighScoreResult(results))
                bundle.putString(FullScreenActivity.VIEW_TYPE, FullScreenActivity.VIEW_TYPE_HIGH_SCORE)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            else -> {

            }
        }
    }

    override fun onResponseListFragmentInteraction(responseTreeNode: ResponseTreeNode) {
        //If there are children, open another ResponseList,
        //Else if there is an explanation, open explanation screen
        if (responseTreeNode.children.size > 0) {
            val fragment = ResponseListFragment.newInstance(responseTreeNode.children.toList() as ArrayList<ResponseTreeNode>, responseTreeNode.startTimeMillis)
            openFragmentWithSlideToLeftEffect(fragment, ResponseListFragment.TAG)
        }
        else if (!responseTreeNode.data.expectedAnswerExplanation.isNullOrEmpty()) {
            val fragment = AnswerExplanationFragment.newInstance(responseTreeNode.data)
            openFragmentWithSlideToLeftEffect(fragment, AnswerExplanationFragment.TAG)
        }

    }

    override fun onResultDetailFragmentInteraction(responses: ArrayList<ResponseTreeNode>?, startTimeMillis: Long, task: Task, action: String) {
        (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForResultsToResponses(task, responses?.isEmpty() == false, action)
        if (responses?.isEmpty() == false) {
            val fragment = ResponseListFragment.newInstance(responses, startTimeMillis)
            openFragmentWithSlideToLeftEffect(fragment, ResponseListFragment.TAG)
        }
        else {
            val alertDialog = (application as StarsEarthApplication)?.createAlertDialog(this)
            alertDialog.setTitle(getString(R.string.error))
            alertDialog.setMessage(getString(R.string.responses_not_recorded))
            alertDialog.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            alertDialog.show()
        }


    }


    override fun onCourseProgressListFragmentInteraction(item: DummyContent.DummyItem?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResultListFragmentInteraction(task: Task?, result: Result?) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForResultListItemTap(task)
        val fragment = ResultDetailFragment.newInstance(task!!, result!!)
        openFragmentWithSlideToLeftEffect(fragment, ResultDetailFragment.TAG)
    }

    override fun onDetailListItemProfilePicTap(imgByteArray: ByteArray) {
        val intent = Intent(this@MainActivity, FullScreenActivity::class.java)
        val bundle = Bundle()
        bundle.putByteArray(FullScreenActivity.IMG_BYTE_ARRAY, imgByteArray)
        bundle.putString(FullScreenActivity.VIEW_TYPE, FullScreenActivity.VIEW_TYPE_PROFILE_PIC)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onDetailListItemTap(itemTitle: DetailListFragment.ListItem, teachingContent: SETeachingContent?, results: ArrayList<Result>) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailListItemTap(itemTitle.toString(), teachingContent)
        when (itemTitle) {
        //Course
            DetailListFragment.ListItem.COURSE_DESCRIPTION -> {
                val fragment = CourseDescriptionFragment.newInstance((teachingContent as Course))
                openFragmentWithSlideToLeftEffect(fragment, CourseDescriptionFragment.TAG)
            }
            DetailListFragment.ListItem.SEE_PROGRESS -> {
                val fragment = CourseProgressListFragment.newInstance((teachingContent as Course), results as ArrayList<Parcelable>)
                openFragmentWithSlideToLeftEffect(fragment, CourseProgressListFragment.TAG)
            }
            DetailListFragment.ListItem.KEYBOARD_TEST -> {
                val intent = Intent(this, KeyboardActivity::class.java)
                startActivity(intent)
            }
            DetailListFragment.ListItem.REPEAT_PREVIOUSLY_PASSED_TASKS -> {
                val fragment = RecordListFragment.newInstance((teachingContent as Course), results as ArrayList<Parcelable>, itemTitle)
                openFragmentWithSlideToLeftEffect(fragment, RecordListFragment.TAG)
            }
            DetailListFragment.ListItem.SEE_RESULTS_OF_ATTEMPTED_TASKS -> {
                val fragment = RecordListFragment.newInstance((teachingContent as Course), results as ArrayList<Parcelable>, itemTitle)
                openFragmentWithSlideToLeftEffect(fragment, RecordListFragment.TAG)
            }

        //Task
            DetailListFragment.ListItem.ALL_RESULTS -> {
                val fragment = ResultListFragment.newInstance((teachingContent as Task), results)
                openFragmentWithSlideToLeftEffect(fragment, ResultListFragment.TAG)
            }
            DetailListFragment.ListItem.HIGH_SCORE -> {
                val fragment = ResultDetailFragment.newInstance((teachingContent as Task), (teachingContent as Task).getHighScoreResult(results))
                openFragmentWithSlideToLeftEffect(fragment, ResultDetailFragment.TAG)
            }

        //All
            DetailListFragment.ListItem.CHANGE_TAGS -> {
                if (mEducator?.status == Educator.Status.ACTIVE && mEducator?.tagging == Educator.PERMISSIONS.TAGGING_ALL) {
                    val fragment = TagListFragment.newInstance(teachingContent as Parcelable)
                    openFragmentWithSlideToLeftEffect(fragment, TagListFragment.TAG)
                }
                else if (mEducator?.status == Educator.Status.ACTIVE
                        && (mEducator?.tagging == Educator.PERMISSIONS.TAGGING_OWN && teachingContent?.creator == FirebaseAuth.getInstance().currentUser?.uid)
                ) {
                    val fragment = TagListFragment.newInstance(teachingContent as Parcelable)
                    openFragmentWithSlideToLeftEffect(fragment, TagListFragment.TAG)
                }
                else if (mEducator?.status == Educator.Status.AUTHORIZED || mEducator?.status == Educator.Status.DEACTIVATED) {
                    val fragment = ProfileEducatorFragment.newInstance(itemTitle, teachingContent as Parcelable)
                    openFragmentWithSlideToLeftEffect(fragment, ProfileEducatorFragment.TAG)
                }
            }
            else -> {

            }
        }
    }

    override fun onDetailFragmentTapInteraction(task: Task) {
        //Only calling this here so that all interactions/transitions are in one place
        if (task.type == Task.Type.SLIDES) {
            val autismStoryFragment = AutismStoryFragment.newInstance(task)
            openFragment(autismStoryFragment, AutismStoryFragment.TAG)
        }
        else if (mUser == null) {
            //redirect to login
            val intent = Intent(this@MainActivity, AddEditPhoneNumberActivity::class.java)
            startActivityForResult(intent, LOGIN_REQUEST)
        }
        else {
            (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailScreenGesture(task, AnalyticsManager.Companion.GESTURES.TAP.toString())
            val intent = Intent(this@MainActivity, TaskTwoActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("task", task)
            intent.putExtras(bundle)
            startActivityForResult(intent, TASK_ACTIVITY_REQUEST)
        }

    }

    override fun onDetailFragmentSwipeInteraction(teachingContent: Any?) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailScreenGesture(teachingContent, AnalyticsManager.Companion.GESTURES.SWIPE.toString())
        val intent = Intent(this, KeyboardActivity::class.java)
        startActivity(intent)
    }

    override fun onDetailFragmentLongPressInteraction(teachingContent: Any?, results: List<Result>) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailScreenGesture(teachingContent, AnalyticsManager.Companion.GESTURES.LONG_PRESS.toString())
        val fragment = DetailListFragment.newInstance(teachingContent as Parcelable?, ArrayList(results))
        openFragmentWithSlideUpEffect(fragment, DetailListFragment.TAG)
    }

    override fun onDetailFragmentShowMessage(errorTitle: String?, errorMessage: String?) {
        val fragment = LastTriedFragment.newInstance(errorTitle, errorMessage)
        openFragment(fragment, LastTriedFragment.TAG)
    }

    override fun onDetailFragmentShowLastTried(teachingContent: Any?, result: Any?) {
        val fragment = LastTriedFragment.newInstance(teachingContent as Parcelable, result as Parcelable?)
        openFragment(fragment, LastTriedFragment.TAG)
    }

    override fun onRecordListItemInteraction(item: RecordItem, index: Int) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForRecordListItemTap(item, index)
        if (item.type == DetailListFragment.ListItem.SEE_RESULTS_OF_ATTEMPTED_TASKS) {
            val fragment = ResultListFragment.newInstance((item.teachingContent as Task))
            openFragmentWithSlideToLeftEffect(fragment, ResultListFragment.TAG)
        }
        else if ((item.teachingContent is Course) && item.results.size < 1) {
            val fragment = CourseDescriptionFragment.newInstance(item.teachingContent as Parcelable, true)
            openFragmentWithSlideToLeftEffect(fragment, CourseDescriptionFragment.TAG)
        }
        else {
            val fragment = DetailFragment.newInstance(item.teachingContent as Parcelable)
            openFragmentWithSlideToLeftEffect(fragment, DetailFragment.TAG)
        }
    }

    override fun onSeOneListFragmentInteraction(item: SEOneListItem, index: Int) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForListItemTap(item.text1, index)
        val intent: Intent
        val type = item.type
        if (type == SEOneListItem.Type.KEYBOARD_TEST) {
            intent = Intent(this, KeyboardActivity::class.java)
            startActivity(intent)
        }
        else if (type == SEOneListItem.Type.PHONE_NUMBER) {
            intent = Intent(this, PhoneNumberActivity::class.java)
            startActivity(intent)
        }
        else if (type == SEOneListItem.Type.EDUCATOR_SEARCH) {
            val searchFragment = SearchFragment.newInstance("EDUCATOR")
            openFragment(searchFragment, SearchFragment.TAG)
        }
        else if (type == SEOneListItem.Type.SEARCH_BY_CLASS) {
            val searchFragment = SearchFragment.newInstance("CLASS")
            openFragment(searchFragment, SearchFragment.TAG)
        }
        else if (type == SEOneListItem.Type.EDUCATOR_PROFILE) {
            val profileEducatorFragment = ProfileEducatorFragment.newInstance()
            openFragment(profileEducatorFragment, ProfileEducatorFragment.TAG)
        }
        else if (type == SEOneListItem.Type.LOGOUT) {
            FirebaseAuth.getInstance().signOut();
            finish()
            intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
        else {
            //val recordsListFragment = RecordListFragment.newInstance(item.type, item.text1)
            //openFragment(recordsListFragment, RecordListFragment.TAG)
            val recordsListFragment = RecordListFragment.newInstance(TagListItem(item.text1) as Parcelable, "TAG")
            openFragment(recordsListFragment, RecordListFragment.TAG)
        }
    }

    private fun openFragment(fragment: Fragment, tag: String) {
        getSupportFragmentManager()?.beginTransaction()
                ?.replace(R.id.fragment_container_main, fragment, tag)
                ?.addToBackStack(tag)
                ?.commit()
    }

    private fun openFragmentWithSlideToLeftEffect(fragment: Fragment, tag: String) {
        getSupportFragmentManager()?.beginTransaction()
                ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                ?.replace(R.id.fragment_container_main, fragment, tag)
                ?.addToBackStack(tag)
                ?.commit()
    }

    private fun openFragmentWithSlideUpEffect(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_up, R.anim.slide_out_to_up)
                .replace(R.id.fragment_container_main, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    private var mAuth: FirebaseAuth? = null
    private val mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
     /*   val user = firebaseAuth.currentUser
        if (user == null) {
            val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }   */
    }

    private val mUserValueChangeListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val key = dataSnapshot?.key
            val value = dataSnapshot?.value as Map<String, Any?>
            if (key != null && value != null) {
                mUser = User(key, value)
            }
        }

        override fun onCancelled(p0: DatabaseError?) {

        }

    }

    private val mEducatorValueChangeListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val map = dataSnapshot?.value
            if (map != null) {
                for (entry in (map as HashMap<*, *>).entries) {
                    val key = entry.key as String
                    val value = entry.value as Map<String, Any?>
                    mEducator = Educator(key, value)
                }

            }
        }

        override fun onCancelled(p0: DatabaseError?) {

        }

    }

    //This is called from any fragment whenever User object is updated
    fun updatedUserProperties() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val firebaseManager = FirebaseManager("users")
            val query = firebaseManager.getQueryForUserObject(it.uid)
            query.addListenerForSingleValueEvent(mUserValueChangeListener)
        }
    }

    //This is called from any fragment whenever Educator object is updated
    private fun updatedEducatorProperties() {
        //Not a guarantee that userid will work. Only on activation will userid be added
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.phoneNumber?.let {
            val firebaseManager = FirebaseManager("educators")
            val query = firebaseManager.getQueryForEducatorsByPhoneNumber(it)
            query.addValueEventListener(mEducatorValueChangeListener)
        }
    }


    var mUser: User? = null //These act as global variables that any fragment can access
    var mEducator : Educator? = null
    var inputAction : String? = null //If there was an inputAction passed into MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        mAuth?.addAuthStateListener(mAuthListener)

        FirebaseAuth.getInstance().currentUser?.let {
            Crashlytics.log("UIID: " + it.uid)
            Crashlytics.log("PHONE NUMBER: " + it.phoneNumber)
        }

        if (mUser == null) {
            updatedUserProperties()
        }
        if (mEducator == null) {
            updatedEducatorProperties()
        }

        val extras = intent.extras
        if (extras?.get("action") == SEOneListItem.Type.EDUCATOR_SEARCH.toString()) {
            inputAction = extras.getString("action")
            val searchFragment = SearchFragment.newInstance("EDUCATOR")
            openFragment(searchFragment, SearchFragment.TAG)
        }
        else {
            val seOneListFragment = SeOneListFragment.newInstance(SEOneListItem.Type.TAG)
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container_main, seOneListFragment).commit()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (supportFragmentManager.backStackEntryCount < 1) {
            //All fragments removed so screen will be blank. Close the activity itself
            if (mUser == null) {
                //No user logged in, close activity and back to login page
                finish()
            }
            else if (mUser != null && inputAction == SEOneListItem.Type.EDUCATOR_SEARCH.toString()){
                //User is logged in and the last fragment was search. That means we came from the search flow on the login screen
                val seOneListFragment = SeOneListFragment.newInstance(SEOneListItem.Type.TAG)
                supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container_main, seOneListFragment).commit()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                val index = supportFragmentManager.backStackEntryCount - 1
                val backEntry = supportFragmentManager.getBackStackEntryAt(index)
                if (backEntry.name == DetailFragment.TAG) {
                    val fragment = supportFragmentManager?.findFragmentByTag(DetailFragment.TAG)
                    (fragment as? DetailFragment)?.onEnterTapped()
                }
                else if (backEntry.name == CourseDescriptionFragment.TAG) {
                    val fragment = supportFragmentManager?.findFragmentByTag(DetailFragment.TAG)
                    (fragment as? CourseDescriptionFragment)?.closeFragment()
                }
                else if (backEntry.name == LastTriedFragment.TAG) {
                    val fragment = supportFragmentManager?.findFragmentByTag(LastTriedFragment.TAG)
                    (fragment as? LastTriedFragment)?.onEnterTapped()
                }
            }
        }

        return super.onKeyUp(keyCode, event)
    }

    val TASK_ACTIVITY_REQUEST = 100
    val LOGIN_REQUEST = 200
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TASK_ACTIVITY_REQUEST) {
            val fragment = supportFragmentManager?.findFragmentByTag(DetailFragment.TAG)
            if (resultCode == Activity.RESULT_CANCELED) {
                (fragment as? DetailFragment)?.onActivityResultCancelled(data)
            }
            if (resultCode == Activity.RESULT_OK) {
                (fragment as? DetailFragment)?.onActivityResultOK(data)
            }
        }
        else if (requestCode == LOGIN_REQUEST) {
            updatedUserProperties()

            //This will be returned to WelcomeActivity whenever LoginActivity closes. This is so that when the user taps back, WelcomeActivity also closes
            val intent = Intent()
            val bundle = Bundle()
            bundle.putBoolean("isLoggedIn", true)
            intent.putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)

            val userId = data?.extras?.getString("userId")
            userId?.let {
                //Have to use Firebase user here as our local copy mUser is not yet updated
                val fragments = supportFragmentManager?.fragments
                (fragments?.getOrNull(fragments.lastIndex) as? DetailFragment)?.onLoginComplete(it)
            }
        }
    }


}
