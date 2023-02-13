package com.starsearth.two.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.starsearth.two.R
import com.starsearth.two.activity.auth.ChangePasswordActivity
import com.starsearth.two.application.StarsEarthApplication
import com.starsearth.two.domain.Response
import com.starsearth.two.domain.Task
import com.starsearth.two.domain.TaskContent
import com.starsearth.two.listeners.SeOnTouchListener
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.activity_task.tvTapScreenToHearContent
import kotlinx.android.synthetic.main.fragment_course_description.*
import java.util.*
import kotlin.collections.HashMap

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TaskActivity : AppCompatActivity(), SeOnTouchListener.OnSeTouchListenerInterface {

    override fun gestureTap() {
        if (mTask.type == Task.Type.TAP_SWIPE) {
            //tap means true
            processGestureResponse()
            if (expectedAnswerGesture) {
                itemsCorrect++
                if (expectedAnswerContentId != null) {
                    responses.add(Response(tvMain.text.toString(),GESTURE_TAP,GESTURE_TAP,true, expectedAnswerContentId))
                }
                else {
                    responses.add(Response(tvMain.text.toString(),GESTURE_TAP,GESTURE_TAP,true))
                }

            }
            else {
                //vibrate() //This vibrates when a wrong answer is given. Instead we will give a vibration half the time is done.
                if (expectedAnswerContentId != null) {
                    responses.add(Response(tvMain.text.toString(),GESTURE_SWIPE,GESTURE_TAP,false, expectedAnswerContentId))
                }
                else {
                    responses.add(Response(tvMain.text.toString(),GESTURE_SWIPE,GESTURE_TAP,false))
                }
            }
            flashAnswerResult(expectedAnswerGesture)
            if (mTask.type == Task.Type.TAP_SWIPE && !mTask.timed && mTask.isTaskItemsCompleted(itemsAttempted)) {
                taskCompleted()
            }
            else {
                updateContent()
            }
        }
        else {
            val mgr = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val volume = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM)
            if (volume > 0) {
                if (mTask.type == Task.Type.SEE_AND_TYPE) {
                    val expectedCharacter = expectedAnswer?.getOrNull(index)
                    expectedCharacter?.toString()?.let { tts?.speak(it, TextToSpeech.QUEUE_ADD, null, "1") }
                }
                else if (expectedAnswerAudioHint != null) {
                    tts?.speak(expectedAnswerAudioHint, TextToSpeech.QUEUE_ADD, null, "1")
                }
                else {
                    tts?.speak(expectedAnswer, TextToSpeech.QUEUE_ADD, null, "1")
                }
            } else {
                val builder = (application as StarsEarthApplication).createAlertDialog(this)
                builder.setTitle(resources.getString(R.string.alert))
                builder.setMessage(resources.getString(R.string.volume_is_mute))
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { dialogInterface, i -> dialogInterface.dismiss() }
                builder.show()
            }

            if (!mTask.isTextVisibleOnStart) {
                //If text is not visible on start, tapping the screen should flash the question to the user
                flashQuestion()
            }
        }
    }

    override fun gestureSwipe() {
        if (mTask.type == Task.Type.TAP_SWIPE) {
            //left -> right or top ->bottom
            //swipe means false
            processGestureResponse()
            if (!expectedAnswerGesture) run {
                itemsCorrect++
                if (expectedAnswerContentId != null) {
                    responses.add(Response(tvMain.text.toString(),GESTURE_SWIPE,GESTURE_SWIPE,true, expectedAnswerContentId))
                }
                else {
                    responses.add(Response(tvMain.text.toString(),GESTURE_SWIPE,GESTURE_SWIPE,true))
                }

            }
            else {
                //vibrate() //This vibrates when a wrong answer is given. Instead we will give a vibration half the time is done.
                if (expectedAnswerContentId != null) {
                    responses.add(Response(tvMain.text.toString(),GESTURE_TAP,GESTURE_SWIPE,false, expectedAnswerContentId))
                }
                else {
                    responses.add(Response(tvMain.text.toString(),GESTURE_TAP,GESTURE_SWIPE,false))
                }
            }
            flashAnswerResult(!expectedAnswerGesture)
            if (mTask.type == Task.Type.TAP_SWIPE && !mTask.timed && mTask.isTaskItemsCompleted(itemsAttempted)) {
                taskCompleted()
            }
            else {
                updateContent()
            }
        }
    }

    override fun gestureLongPress() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val GESTURE_SWIPE = "GESTURE_SWIPE"
    val GESTURE_TAP = "GESTURE_TAP"
    var QUESTION_SPELL_IGNORE_CASE = "QUESTION_SPELL_IGNORE_CASE"
    var QUESTION_TYPE_CHARACTER = "QUESTION_TYPE_CHARACTER"

    private lateinit var mTask : Task

    private lateinit var tts: TextToSpeech

    private var mCountDownTimer: CountDownTimer? = null
    private var startTimeMillis: Long = 0
    private var timeTakenMillis : Long = 0
    private val responses = ArrayList<Response>()

    //typing activity
    private var index = 0
    private var charactersCorrect: Long = 0
    private var charactersTotalAttempted: Long = 0
    private var wordsCorrect: Long = 0
    private var wordsTotalFinished: Long = 0
    private var wordIncorrect = false //This is used to show that 1 mistake has been made when typing a word
    private var expectedAnswer: String? = null
    private var expectedAnswerAudioHint: String? = null

    //gesture activity
    private var expectedAnswerGesture: Boolean = false
    private var expectedAnswerContentId: String? = null
    private var itemsAttempted: Long = 0              //In SEE_AND_TYPE, only used to see how many have been completed
    private var itemsCorrect: Long = 0
    private var itemIncorrect = false  //This is used to show that 1 mistake has been made when typing an item(character/word/sentence)
    private var gestureSpamItemCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_task)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)


        intent?.extras?.let {
            mTask = it.getParcelable(TASK)!!
        }
        setupUI()
        startTimeMillis = System.currentTimeMillis()
        if (mTask.timed) {
            setupTimer(mTask.durationMillis.toLong(), 1000)
        }
        //cl?.setOnTouchListener(SeOnTouchListener(this)) //Sept 2022: Using buttons instead
    }

    override fun onStart() {
        super.onStart()

        if ((application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
            tvTimer?.text = "Talkback is ON. Timer will not be displayed. But it will run in background and voice notifications will be given"
            tvTimer?.setTextColor(Color.GRAY)
        }

        tts = TextToSpeech(this, null)
        tts.setLanguage(Locale.US)

        //If isKeyboardRequred flag has been missed, can also judge from the type
        if (mTask.isKeyboardRequired || mTask.type == Task.Type.SEE_AND_TYPE || mTask.type == Task.Type.HEAR_AND_TYPE) {
            cl.postDelayed({
                cl?.requestFocus()
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(cl, 0)
            }, 500)
        }
        btnTrue?.setOnClickListener { gestureTap() }
        btnFalse?.setOnClickListener { gestureSwipe() }
        btnSayTimeLeft?.setOnClickListener {
            val millisUntilFinished = 61000 - timeTakenMillis
            val secsUntilFinished = millisUntilFinished / 1000
            tts?.speak(secsUntilFinished.toString() + " seconds", TextToSpeech.QUEUE_ADD, null, "1")
        }
        btnSayQuestion?.setOnClickListener { tts?.speak(tvQuestion?.text?.toString(), TextToSpeech.QUEUE_ADD, null, "1") }
        btnRepeatStatement?.setOnClickListener { tts?.speak(tvMain?.text?.toString(), TextToSpeech.QUEUE_ADD, null, "1") }
        updateContent()
    }

    override fun onStop() {
        super.onStop()
        tts?.shutdown()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        taskCancelled(Task.BACK_PRESSED)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (mTask.isExitOnInterruption) {
            taskCancelled(Task.HOME_BUTTON_TAPPED)
        }
    }

    private fun beep() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 200 milliseconds
        //v.vibrate(200)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(200)
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

                if ((millisUntilFinished / 1000).toInt() == (((mTask.durationMillis - 1000)/1000)/2)) {
                    //at the halfway mark
                    vibrate()
                    Toast.makeText(this@TaskActivity, "Half time finished",
                        Toast.LENGTH_SHORT).show()
                }
                if (millisUntilFinished.toInt() / 1000 == 10) {
                    //Only when its 10. Not every number after 10
                    Toast.makeText(this@TaskActivity, "10 seconds left",
                        Toast.LENGTH_SHORT).show()
                }

                //when talkback is on, it speaks everytime the llabel is updated with a new  time.  We dont  want  that.  So only if talklbaack is off will we update
                if ((application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == false) {
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


            }

            override fun onFinish() {
                timeTakenMillis = timeTakenMillis + 1000 //take the last second into consideration
                if (mTask.getType() == Task.Type.SEE_AND_TYPE && charactersTotalAttempted == 0L || mTask.getType() == Task.Type.TAP_SWIPE && itemsAttempted == 0L) {
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
    }

    private fun setupUIVisibility() {
        tvCompletedTotal?.visibility =
                if (!mTask.timed) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        tvTapScreenToHearContent?.visibility = View.GONE
           /*     if (!mTask.isTextVisibleOnStart) {
                    View.VISIBLE
                } else {
                    View.GONE
                }   */

        tvTimer?.visibility =
                if (mTask.timed) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        tvTapSwipeKeyboardInstructions?.visibility = View.GONE
            /*    if (mTask.type == Task.Type.TAP_SWIPE) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }   */
        llActivityActions?.visibility =
                if (mTask.type == Task.Type.TAP_SWIPE) {
                    View.VISIBLE
                }
                else {
                    View.GONE
                }

    }

    private fun setupUIText() {
        tvQuestion?.text = mTask.instructions

        tvCompletedTotal?.text =
                if (!mTask.timed && mTask.content.size > 0) {
                    "1" + "/" + mTask.content.size
                } else {
                    ""
                }

     /*   tvTapScreenToHearContent?.text =
                if ((application as? StarsEarthApplication)?.accessibilityManager?.isTalkbackOn == true) {
                    getString(R.string.double_tap_screen_to_hear_text_again)
                } else {
                    getString(R.string.tap_screen_to_hear_text_again)
                }   */ //Not needed
    }

    private fun updateContent() {
        index = 0
        val nextItem = if (mTask.ordered) {
                            mTask.getNextItem(itemsAttempted.toInt())
                        }
                        else {
                            mTask.nextItem
                        }
        if (nextItem is String) {
            expectedAnswer = nextItem.replace("␣", " ")
            if (mTask.isTextVisibleOnStart) {
                tvMain?.text = nextItem
                android.os.Handler().postDelayed({
                    //If it is the first content after activity open, give it a 1 second delay so that TalkBack can announce all other things
                    val substring = nextItem.substring(0, 1)
                    if (substring == " ") {
                        tvMain?.announceForAccessibility(getString(R.string.next_character) + " " + getString(R.string.space))
                    }
                    else {
                        tvMain?.announceForAccessibility(getString(R.string.next_character) + " " + substring)
                    }
                },
                        1000)

            }
            else {
                tvMain?.announceForAccessibility(getString(R.string.double_tap_for_next_item))
                tvMain?.text = ""
            }
        }
        else if (nextItem is TaskContent) {
            expectedAnswer = nextItem.question
            expectedAnswerGesture = nextItem.isTrue
            expectedAnswerContentId = nextItem.id
            expectedAnswerAudioHint = nextItem.hintAudio
            if (mTask.isTextVisibleOnStart) {
                tvMain?.text = nextItem.question
                llMain?.contentDescription = nextItem.question
            }
            else {
                tvMain?.text = ""
            }
            android.os.Handler().postDelayed({
                //If it is the first content after activity open, give it a 1 second delay so that TalkBack can announce all other things
                tvMain.announceForAccessibility(nextItem.question as String)
            },
                    1000)
        }
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

    private fun flashAnswerResult(isCorrect: Boolean) {
        tvMain?.announceForAccessibility(
                if (isCorrect) {
                    getString(R.string.correct)
                } else {
                    getString(R.string.not_correct)
                }
        )

        val imageView : ImageView = if (isCorrect) {
                            ivGreen
                        } else {
                            ivRed
                        }
        imageView?.alpha = 0f
        imageView?.visibility = View.VISIBLE

        imageView?.animate()
                ?.alpha(1f)
                ?.setDuration(400)
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        imageView?.visibility = View.GONE
                    }
                })
    }

    /*
        If question is not visible to user, this will flash the question for a few seconds
     */
    private fun flashQuestion() {
        tvHint?.alpha = 0f
        tvHint?.text = expectedAnswer
        tvHint?.visibility = View.VISIBLE

        tvHint?.animate()
                ?.alpha(1f)
                ?.setDuration(2000)
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        tvHint?.text = ""
                        tvHint?.visibility = View.GONE
                    }
                })
    }

    /*
        Currently used to show backspace not allowed
     */
    private fun flashWarning() {
        val imageView : ImageView = ivRed
        imageView?.alpha = 0f
        imageView?.visibility = View.VISIBLE

        imageView?.animate()
                ?.alpha(1f)
                ?.setDuration(150)
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        imageView?.visibility = View.GONE
                    }
                })
    }

    private fun checkWordCorrect() {
        //You must also tap the spacebar after the word to get the word correct
        if (!wordIncorrect) {
            //if the word was not declared incorrect, increment the words correct count
            wordsCorrect++
        }
        wordIncorrect = false //reset the flag for the next word
    }

    //Only used in type = SEE_AND_TYPE
    private fun checkItemCorrect() {
        if (!itemIncorrect) {
            //if NO characters in item were declared incorrect, increment the items correct count
            itemsCorrect++
        }
        itemIncorrect = false //reset the flag for the next word
    }

    private fun hasReachedEndOfWord(inputCharacter: Char?) : Boolean {
        return (inputCharacter == ' ' || index == expectedAnswer?.length?.minus(1)) && !mTask.submitOnReturnTapped
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
    //Results from FirebaseManager have Integer as Long
    private fun taskCompleted() {
        (applicationContext as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTaskComplete()
        mCountDownTimer?.cancel()
        val map = HashMap<String, Any>()
        map["task_id"] = if (mTask.uid != null) { mTask.uid } else { mTask.id }
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


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_SHIFT_LEFT,
            KeyEvent.KEYCODE_SHIFT_RIGHT,
            KeyEvent.KEYCODE_CAPS_LOCK ->
                //Ignore these
                return true
            KeyEvent.KEYCODE_DEL ->
                if (mTask.isBackspaceAllowed) {
                    tvMain?.text = tvMain.text.subSequence(0, tvMain.text.length - 1)
                    tvMain?.announceForAccessibility(getString(R.string.backspace))
                    return super.onKeyDown(keyCode, event)
                }
                else {
                    //Backspace not allowed, signal error.
                    beep()
                    vibrate()
                    flashWarning()
                    tvMain?.announceForAccessibility(getString(R.string.backspace_not_allowed))
                    return super.onKeyDown(keyCode, event)
                }
            KeyEvent.KEYCODE_ENTER ->
                if (mTask.type == Task.Type.HEAR_AND_TYPE) {
                    itemsAttempted++
                    if (mTask.ordered) {
                        tvCompletedTotal.text = (itemsAttempted + 1).toString() + "/" + mTask.content.size
                    }
                    tvMain?.text?.toString()?.let {
                        val isCorrect = it.equals(expectedAnswer, true)
                        if (isCorrect) itemsCorrect++
                        flashAnswerResult(isCorrect)
                        responses.add(Response(QUESTION_SPELL_IGNORE_CASE,expectedAnswer,it,isCorrect))

                        //For spelling tasks we only submit on return tapped
                        if (mTask.type == Task.Type.HEAR_AND_TYPE && !mTask.timed && mTask.isTaskItemsCompleted(itemsAttempted)) {
                            taskCompleted()
                        }
                        else if (mTask.type == Task.Type.HEAR_AND_TYPE && !mTask.timed && !mTask.isTaskItemsCompleted(itemsAttempted)) {
                            updateContent()
                        }
                    }
                    return super.onKeyDown(keyCode, event)

                }
                else {
                    return super.onKeyDown(keyCode, event)
                }
            else -> {
                //All other characters
                charactersTotalAttempted++
                val inputCharacter = event?.unicodeChar?.toChar()
                if (inputCharacter != null) {
                    if (inputCharacter == ' ') {
                        tvMain?.announceForAccessibility(getString(R.string.space))
                    }
                    else {
                        tvMain?.announceForAccessibility(Character.toString(inputCharacter))
                    }
                }
                if (mTask.type == Task.Type.HEAR_AND_TYPE) {
                    tvMain?.text = tvMain?.text?.toString() + inputCharacter
                }
                else if (mTask.type == Task.Type.SEE_AND_TYPE) {
                    val expectedCharacter = expectedAnswer?.getOrNull(index)
                    if (expectedCharacter == null) {
                        return super.onKeyDown(keyCode, event) //Exit the flow.
                    }
                    val isCorrect = inputCharacter == expectedCharacter
                    if (isCorrect) {
                        charactersCorrect++

                    }
                    else {
                        itemIncorrect = true
                        wordIncorrect = true
                    }
                    if (expectedCharacter != null && inputCharacter != null) {
                        responses.add(Response(QUESTION_TYPE_CHARACTER,Character.toString(expectedCharacter),Character.toString(inputCharacter),isCorrect))
                    }

                    val spannableString = SpannableString(tvMain?.text.toString())
                    spannableString.setSpan(BackgroundColorSpan(if (isCorrect) {
                                                    Color.GREEN
                                                } else {
                                                    Color.RED
                                                }), index, index + 1, 0)
                    tvMain?.announceForAccessibility(
                            if (isCorrect) {
                                getString(R.string.correct)
                            } else {
                                getString(R.string.not_correct)
                            }
                    )
                    tvMain?.setText(spannableString, TextView.BufferType.SPANNABLE)

                    //Reached the last character in the the expected answer
                    if (index == expectedAnswer?.length?.minus(1)) {
                        itemsAttempted++
                        checkItemCorrect()
                        if (!mTask.timed) {
                            tvCompletedTotal?.text = (itemsAttempted + 1).toString() + "/" + mTask.content.size
                        }
                    }

                    //Prepare for next item
                    index++

                    if (mTask.isTextVisibleOnStart && index < expectedAnswer?.length!!) {
                        //If we have not yet reached the end and the text is visible to the user
                        //announce next character for accessibility, index has been incremented
                        //do it only if text is visible on start
                        val nextExpectedCharacter = expectedAnswer?.getOrNull(index)
                        if (nextExpectedCharacter == ' ') {
                            tvMain.announceForAccessibility(getString(R.string.next_character) + " " + getString(R.string.space))
                        } else if (nextExpectedCharacter == '.') {
                            tvMain.announceForAccessibility(getString(R.string.next_character) + " " + getString(R.string.full_stop))
                        } else {
                            tvMain.announceForAccessibility(getString(R.string.next_character) + " " + nextExpectedCharacter.toString())
                        }
                    }
                }
                else if (mTask.type == Task.Type.TAP_SWIPE) {
                    if (inputCharacter?.equals('y', ignoreCase = true) == true) {
                        processGestureResponse()
                        if (expectedAnswerGesture) {
                            itemsCorrect++
                        }
                        flashAnswerResult(expectedAnswerGesture)
                        if (expectedAnswerContentId != null) {
                            responses.add(Response(tvMain.text.toString(),if (expectedAnswerGesture) {
                                GESTURE_TAP
                            } else {
                                GESTURE_SWIPE
                            },GESTURE_TAP, expectedAnswerGesture, expectedAnswerContentId))  //Answer was true. If expected was true send true, else send false
                        }
                        else {
                            responses.add(Response(tvMain.text.toString(),if (expectedAnswerGesture) {
                                GESTURE_TAP
                            } else {
                                GESTURE_SWIPE
                            },GESTURE_TAP, expectedAnswerGesture))  //Answer was true. If expected was true send true, else send false
                        }

                    }
                    else if (inputCharacter?.equals('n', ignoreCase = true) == true) {
                        processGestureResponse()
                        if (!expectedAnswerGesture) {
                            itemsCorrect++
                        }
                        flashAnswerResult(!expectedAnswerGesture)
                        if (expectedAnswerContentId != null) {
                            responses.add(Response(tvMain.text.toString(),if (expectedAnswerGesture) {
                                GESTURE_TAP
                            } else {
                                GESTURE_SWIPE
                            },GESTURE_SWIPE, !expectedAnswerGesture, expectedAnswerContentId)) //Answer was false. If expected was false, send true
                        }
                        else {
                            responses.add(Response(tvMain.text.toString(),if (expectedAnswerGesture) {
                                GESTURE_TAP
                            } else {
                                GESTURE_SWIPE
                            },GESTURE_SWIPE, !expectedAnswerGesture)) //Answer was false. If expected was false, send true
                        }

                    }
                    else if (inputCharacter?.equals(' ', true) == true) {
                        //If space was tapped, say the content on the screen
                        tts?.speak(tvMain?.text?.toString(), TextToSpeech.QUEUE_ADD, null, "1")
                        return super.onKeyDown(keyCode, event) //Exit the flow. We simply want to say what is on the screen, nothing else
                    }
                    else {
                        return super.onKeyDown(keyCode, event) //Exit the flow. If it is any other character apart from the y,n or space, we do nothing
                    }
                }

                //Check if we have reached the end of a word
                if (mTask.type == Task.Type.SEE_AND_TYPE && hasReachedEndOfWord(inputCharacter)) {
                    //only consider this when submit on enter is not selected
                    wordsTotalFinished++ //on spacebar, or on end of string, we have completed a word
                    checkWordCorrect()
                }
            }

        }


        android.os.Handler().postDelayed({
                    //One millis delay so user can see the result of last letter before sentence changes
                    if (mTask.type == Task.Type.SEE_AND_TYPE && !mTask.timed && mTask.isTaskItemsCompleted(itemsAttempted)) {
                        taskCompleted()
                    }
                    else if (mTask.type == Task.Type.TAP_SWIPE && !mTask.timed && mTask.isTaskItemsCompleted(itemsAttempted)) {
                        taskCompleted()
                    }
                    else if (mTask.type == Task.Type.SEE_AND_TYPE && !mTask.submitOnReturnTapped && index == expectedAnswer?.length) {
                        updateContent()
                    }
                    else if (mTask.type == Task.Type.TAP_SWIPE) {
                        updateContent()
                    }

                },
                100)

        return super.onKeyDown(keyCode, event)


    }

    private fun processGestureResponse() {
        itemsAttempted++
        if (mTask.ordered) {
            tvCompletedTotal.text = (itemsAttempted + 1).toString() + "/" + mTask.content.size
        }
        gestureSpamItemCounter++
    }


    companion object {
        val TASK = "task"
    }

}
