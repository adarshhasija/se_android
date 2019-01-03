package com.starsearth.one.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

import com.starsearth.one.R
import com.starsearth.one.activity.profile.PhoneNumberActivity
import com.starsearth.one.activity.welcome.WelcomeOneActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.*
import com.starsearth.one.fragments.LastTriedFragment
import com.starsearth.one.fragments.ResultDetailFragment
import com.starsearth.one.fragments.TaskDetailFragment
import com.starsearth.one.fragments.dummy.DummyContent
import com.starsearth.one.fragments.lists.*
import com.google.firebase.auth.FirebaseUser
import android.support.annotation.NonNull



class MainActivity : AppCompatActivity(),
        RecordListFragment.OnRecordListFragmentInteractionListener,
        TaskDetailFragment.OnTaskDetailFragmentInteractionListener,
        TaskDetailListFragment.OnTaskDetailListFragmentListener,
        ResultListFragment.OnResultListFragmentInteractionListener,
        CourseProgressListFragment.OnCourseProgressListFragmentInteractionListener,
        ResultDetailFragment.OnResultDetailFragmentInteractionListener,
        ResponseListFragment.OnResponseListFragmentInteractionListener,
        SeOneListFragment.OnSeOneListFragmentInteractionListener {

    override fun onResponseListFragmentInteraction(item: Response?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onResultDetailFragmentInteraction(result: Any) {
        val fragment = ResponseListFragment.newInstance(result)
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }


    override fun onCourseProgressListFragmentInteraction(item: DummyContent.DummyItem?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResultListFragmentInteraction(task: Task?, result: Result?) {
        val fragment = ResultDetailFragment.newInstance(task!!, result!!)
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onTaskDetailListFragmentInteraction(itemTitle: TaskDetailListFragment.LIST_ITEM, teachingContent: Any?, results: ArrayList<Result>) {
        when (itemTitle) {
        //Course
            TaskDetailListFragment.LIST_ITEM.SEE_PROGRESS -> {
                val fragment = CourseProgressListFragment.newInstance((teachingContent as Course), results as ArrayList<Parcelable>)
                getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
            TaskDetailListFragment.LIST_ITEM.KEYBOARD_TEST -> {
                val intent = Intent(this, KeyboardActivity::class.java)
                startActivity(intent)
            }
            TaskDetailListFragment.LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS -> {
                val fragment = RecordListFragment.newInstance((teachingContent as Course), results as ArrayList<Parcelable>, TaskDetailListFragment.LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS)
                getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }

        //Task
            TaskDetailListFragment.LIST_ITEM.ALL_RESULTS -> {
                val fragment = ResultListFragment.newInstance((teachingContent as Task), results)
                getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
            TaskDetailListFragment.LIST_ITEM.HIGH_SCORE -> {
                val fragment = ResultDetailFragment.newInstance((teachingContent as Task), (teachingContent as Task).getHighScoreResult(results))
                getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
            else -> {

            }
        }
    }

    override fun onTaskDetailFragmentSwipeInteraction(teachingContent: Any?) {
        val intent = Intent(this, KeyboardActivity::class.java)
        startActivity(intent)
    }

    override fun onTaskDetailFragmentLongPressInteraction(teachingContent: Any?, results: List<Result>) {
        val fragment = TaskDetailListFragment.newInstance(teachingContent as Parcelable?, ArrayList(results))
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_up, R.anim.slide_out_to_up)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onTaskDetailFragmentShowLastTried(teachingContent: Any?, result: Any?, errorTitle: String?, errorMessage: String?) {
        val fragment = LastTriedFragment.newInstance(teachingContent as Parcelable, result as Parcelable?, errorTitle, errorMessage)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onRecordListItemInteraction(item: RecordItem) {
        val fragment = TaskDetailFragment.newInstance(item.teachingContent as Parcelable)
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onSeOneListFragmentInteraction(item: SEOneListItem) {
        sendAnalytics(item.text1)
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
        else if (type == SEOneListItem.Type.LOGOUT) {
            FirebaseAuth.getInstance().signOut();
            finish()
            intent = Intent(this, WelcomeOneActivity::class.java)
            startActivity(intent)
        }
        else {
            val recordsListFragment = RecordListFragment.newInstance(item.type, item.text1)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_main, recordsListFragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    fun sendAnalytics(selected: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected)
        (application as? StarsEarthApplication)?.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    private var mAuth: FirebaseAuth? = null
    private val mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user == null) {
            val intent = Intent(this@MainActivity, WelcomeOneActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance();
        mAuth?.addAuthStateListener(mAuthListener);

        val seOneListFragment = SeOneListFragment.newInstance(SEOneListItem.Type.TAG)
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container_main, seOneListFragment).commit()
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




}
