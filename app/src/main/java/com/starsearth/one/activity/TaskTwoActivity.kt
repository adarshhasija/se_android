package com.starsearth.one.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.starsearth.one.R
import com.starsearth.one.activity.FullScreenActivity.Companion.TASK
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.Task
import kotlinx.android.synthetic.main.activity_task_two.*
import java.util.HashMap

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TaskTwoActivity : AppCompatActivity() {

    private lateinit var mTask : Task

    private var mCountDownTimer: CountDownTimer? = null
    private var startTimeMillis: Long = 0
    private var timeTakenMillis : Long = 0

    //typing activity
    private var index = 0
    private var charactersCorrect: Long = 0
    private var charactersTotalAttempted: Long = 0
    private var wordsCorrect: Long = 0
    private var wordsTotalFinished: Long = 0
    private var wordIncorrect = false //This is used to show that 1 mistake has been made when typing a word
    private var expectedAnswer: String? = null //for typing tasks

    //gesture activity
    private var expectedAnswerGesture: Boolean = false
    private var itemsAttempted: Long = 0              //In TYPING, only used to see how many have been completed
    private var itemsCorrect: Long = 0
    private var itemIncorrect = false  //This is used to show that 1 mistake has been made when typing an item(character/word/sentence)
    private var gestureSpamItemCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_task_two)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)


        intent?.extras?.let {
            mTask = it.getParcelable(TASK)
        }
        setupUI()
        startTimeMillis = System.currentTimeMillis()
        if (mTask.timed) {
            setupTimer(mTask.durationMillis.toLong(), 1000)
        }
    }

    private fun setupTimer(duration: Long, interval: Long) {
        mCountDownTimer = object : CountDownTimer(duration, interval) {

            override fun onTick(millisUntilFinished: Long) {
                timeTakenMillis = 61000 - millisUntilFinished
                if (millisUntilFinished / 1000 % 10 == 0L) {
                    if (gestureSpamItemCounter > 30) {
                        taskCancelled(Task.GESTURE_SPAM)
                    }
                    gestureSpamItemCounter = 0
                }

                if (millisUntilFinished / 1000 < 11) {
                    tvTimer?.setTextColor(Color.RED)
                }

                if (millisUntilFinished / 1000 < 10) {
                    tvTimer?.setText((millisUntilFinished / 1000 / 60).toString() + ":0" + millisUntilFinished / 1000)
                } else {
                    val mins = (millisUntilFinished / 1000).toInt() / 60
                    val seconds = (millisUntilFinished / 1000).toInt() % 60
                    tvTimer?.setText(mins.toString() + ":" + if (seconds == 0) "00" else seconds) //If seconds are 0, print double 0, else print seconds
                }


            }

            override fun onFinish() {
                timeTakenMillis = timeTakenMillis + 1000 //take the last second into consideration
                if (mTask.getType() == Task.Type.TYPING && charactersTotalAttempted == 0L || mTask.getType() == Task.Type.TAP_SWIPE && itemsAttempted == 0L) {
                    taskCancelled(Task.NO_ATTEMPT)
                } else {
                    taskCompleted()
                }
            }
        }.start()
    }

    private fun setupUI() {
        setupUIVisibility()
        setupUIText()
        setupUIAccessibility()
    }

    private fun setupUIVisibility() {
        tvCompletedTotal?.visibility =
                if (!mTask.timed) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        tvTapScreenToHearContent?.visibility =
                if (!mTask.isTextVisibleOnStart) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        tvTimer?.visibility =
                if (mTask.timed) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

    }

    private fun setupUIText() {
        tvCompletedTotal?.text =
                if (!mTask.timed && mTask.content.size > 0) {
                    "1" + "/" + mTask.content.size
                } else {
                    ""
                }

        tvTapScreenToHearContent?.text =
                if ((application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                    getString(R.string.double_tap_screen_to_hear_text_again)
                } else {
                    getString(R.string.tap_screen_to_hear_text_again)
                }
    }

    private fun setupUIAccessibility() {

    }

    private fun taskCancelled(reason: String) {
        (applicationContext as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTaskCancellation(mTask, reason)
        endTask(reason)
    }

    private fun endTask(reason: String) {
        mCountDownTimer?.cancel()
        val bundle = Bundle()
        val intent = Intent()
        bundle.putString(Task.FAIL_REASON, reason)
        intent.putExtras(bundle)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    //If timed activity, return timeTakenMillis
    //If untimed activity, calculate the time taken
    private fun calculateTimeTaken(): Long {
        if (!mTask.timed) {
            timeTakenMillis = System.currentTimeMillis() - startTimeMillis
        }
        return timeTakenMillis
    }

    //Integers must be saved as Long
    //Results constructor takes values as Long
    //Results from Firebase have Integer as Long
    private fun taskCompleted() {
        mCountDownTimer?.cancel()
        val map = HashMap<String, Any>()
        map["task_id"] = mTask.id
        map["taskTypeLong"] = mTask.type.getValue()
        map["startTimeMillis"] = startTimeMillis
        map["timeTakenMillis"] = calculateTimeTaken()
        map["items_correct"] = itemsCorrect
        map["items_attempted"] = itemsAttempted
        map["characters_correct"] = charactersCorrect
        map["characters_total_attempted"] = charactersTotalAttempted
        map["words_correct"] = wordsCorrect
        map["words_total_finished"] = wordsTotalFinished
        map["responses"] = responses

        val bundle = Bundle()
        bundle.putSerializable("result_map", map)
        setResult(Activity.RESULT_OK, Intent().putExtras(bundle))
        finish()
    }


    companion object {
        val TASK = "task"
    }

}
