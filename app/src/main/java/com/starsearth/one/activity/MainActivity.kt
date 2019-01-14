package com.starsearth.one.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth

import com.starsearth.one.R
import com.starsearth.one.activity.profile.PhoneNumberActivity
import com.starsearth.one.activity.tasks.TaskActivity
import com.starsearth.one.activity.welcome.WelcomeOneActivity
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.*
import com.starsearth.one.fragments.LastTriedFragment
import com.starsearth.one.fragments.ResultDetailFragment
import com.starsearth.one.fragments.DetailFragment
import com.starsearth.one.fragments.dummy.DummyContent
import com.starsearth.one.fragments.lists.*
import com.starsearth.one.managers.AnalyticsManager


class MainActivity : AppCompatActivity(),
        RecordListFragment.OnRecordListFragmentInteractionListener,
        DetailFragment.OnDetailFragmentInteractionListener,
        DetailListFragment.OnTaskDetailListFragmentListener,
        ResultListFragment.OnResultListFragmentInteractionListener,
        CourseProgressListFragment.OnCourseProgressListFragmentInteractionListener,
        ResultDetailFragment.OnResultDetailFragmentInteractionListener,
        ResponseListFragment.OnResponseListFragmentInteractionListener,
        SeOneListFragment.OnSeOneListFragmentInteractionListener {

    override fun onDetailListItemLongPress(itemTitle: DetailListFragment.LIST_ITEM, teachingContent: SETeachingContent?, results: ArrayList<Result>) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailListItemLongPress(itemTitle.toString(), teachingContent)
        when (itemTitle) {
            DetailListFragment.LIST_ITEM.HIGH_SCORE -> {
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

    override fun onResponseListFragmentInteraction(item: Response?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onResultDetailFragmentInteraction(result: Result, task: Task, action: String) {
        (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForResultsToResponses(task, result.responses.size > 0, AnalyticsManager.Companion.GESTURES.LONG_PRESS.toString())
        if (result.responses != null && result.responses.size > 0) {
            val fragment = ResponseListFragment.newInstance(result)
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                    .replace(R.id.fragment_container_main, fragment, ResultDetailFragment.TAG)
                    .addToBackStack(ResultDetailFragment.TAG)
                    .commit()
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResultListFragmentInteraction(task: Task?, result: Result?) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForResultListItemTap(task)
        val fragment = ResultDetailFragment.newInstance(task!!, result!!)
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onDetailListItemTap(itemTitle: DetailListFragment.LIST_ITEM, teachingContent: SETeachingContent?, results: ArrayList<Result>) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailListItemTap(itemTitle.toString(), teachingContent)
        when (itemTitle) {
        //Course
            DetailListFragment.LIST_ITEM.SEE_PROGRESS -> {
                val fragment = CourseProgressListFragment.newInstance((teachingContent as Course), results as ArrayList<Parcelable>)
                getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
            DetailListFragment.LIST_ITEM.KEYBOARD_TEST -> {
                val intent = Intent(this, KeyboardActivity::class.java)
                startActivity(intent)
            }
            DetailListFragment.LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS -> {
                val fragment = RecordListFragment.newInstance((teachingContent as Course), results as ArrayList<Parcelable>, DetailListFragment.LIST_ITEM.REPEAT_PREVIOUSLY_PASSED_TASKS)
                getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }

        //Task
            DetailListFragment.LIST_ITEM.ALL_RESULTS -> {
                val fragment = ResultListFragment.newInstance((teachingContent as Task), results)
                getSupportFragmentManager()?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                        ?.replace(R.id.fragment_container_main, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
            DetailListFragment.LIST_ITEM.HIGH_SCORE -> {
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

    override fun onDetailFragmentTapInteraction(task: Task) {
        //Only calling this here so that all interactions/transitions are in one place
     /*   (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailScreenGesture(task, AnalyticsManager.Companion.GESTURES.TAP.toString())
        val intent = Intent(this@MainActivity, TaskActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable("task", task)
        intent.putExtras(bundle)
        startActivityForResult(intent, TASK_ACTIVITY_REQUEST)   */
        val intent = Intent(this@MainActivity, TaskTwoActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable("task", task)
        intent.putExtras(bundle)
        startActivityForResult(intent, TASK_ACTIVITY_REQUEST)
    }

    override fun onDetailFragmentSwipeInteraction(teachingContent: Any?) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailScreenGesture(teachingContent, AnalyticsManager.Companion.GESTURES.SWIPE.toString())
        val intent = Intent(this, KeyboardActivity::class.java)
        startActivity(intent)
    }

    override fun onDetailFragmentLongPressInteraction(teachingContent: Any?, results: List<Result>) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForDetailScreenGesture(teachingContent, AnalyticsManager.Companion.GESTURES.LONG_PRESS.toString())
        val fragment = DetailListFragment.newInstance(teachingContent as Parcelable?, ArrayList(results))
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_up, R.anim.slide_out_to_up)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onDetailFragmentShowMessage(errorTitle: String?, errorMessage: String?) {
        val fragment = LastTriedFragment.newInstance(errorTitle, errorMessage)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onDetailFragmentShowLastTried(teachingContent: Any?, result: Any?) {
        val fragment = LastTriedFragment.newInstance(teachingContent as Parcelable, result as Parcelable?)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onRecordListItemInteraction(item: RecordItem, index: Int) {
        (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForRecordListItemTap(item, index)
        val fragment = DetailFragment.newInstance(item.teachingContent as Parcelable)
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                .replace(R.id.fragment_container_main, fragment, DetailFragment.FRAGMENT_TAG)
                .addToBackStack(DetailFragment.FRAGMENT_TAG)
                .commit()
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

    val TASK_ACTIVITY_REQUEST = 100
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TASK_ACTIVITY_REQUEST) {
            val fragment = supportFragmentManager?.findFragmentByTag(DetailFragment.FRAGMENT_TAG)
            if (resultCode == Activity.RESULT_CANCELED) {
                (fragment as? DetailFragment)?.onActivityResultCancelled(data)
            }
            if (resultCode == Activity.RESULT_OK) {
                (fragment as? DetailFragment)?.onActivityResultOK(data)
            }
        }
    }


}
