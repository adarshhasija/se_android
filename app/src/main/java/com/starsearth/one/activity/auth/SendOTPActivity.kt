package com.starsearth.one.activity.auth

import android.app.Activity
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import java.util.concurrent.TimeUnit
//import jdk.nashorn.internal.runtime.ECMAException.getException
//import org.junit.experimental.results.ResultMatchers.isSuccessful
import com.google.android.gms.tasks.Task
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.starsearth.one.R
import com.starsearth.one.application.StarsEarthApplication


class SendOTPActivity : AppCompatActivity() {

    var TAG = "SendOTPActivity"

    private var phoneNumber: String? = null
    private var mVerificationId: String? = null
    private var mToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mAuth: FirebaseAuth? = null


    private var mCountDownTimer: CountDownTimer? = null
    private var mTimer: TextView? = null
    private var mViewSendOTPAgain: LinearLayout? = null
    private var mViewOTPTimer: LinearLayout? = null
    private var mViewPleaseWait: LinearLayout? = null

    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:" + credential);

            signInWithPhoneAuthCredential(credential);
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }
            val builder = createAlertDialog()
            builder.setTitle(R.string.error)
                    .setMessage(e.message)
                    .setNeutralButton(android.R.string.ok) { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
        }

        override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
            super.onCodeSent(verificationId, token)
            mVerificationId = verificationId
            mToken = token
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_otp)

        mVerificationId = savedInstanceState?.getString("verificationId")
        mAuth = FirebaseAuth.getInstance()

        val etOTP = findViewById<EditText>(R.id.et_otp) as EditText

        val btnSubmit = findViewById<Button>(R.id.btn_submit) as Button
        btnSubmit.setOnClickListener { v: View? ->
            var otp = etOTP.text.toString()
            if (!isFormatIncorrect(otp)) {
                val credential = PhoneAuthProvider.getCredential(this.mVerificationId!!, otp)
                signInWithPhoneAuthCredential(credential)
            }
        }

        val btnSendOTPAgain = findViewById<Button>(R.id.btn_send_otp_again) as Button
        btnSendOTPAgain.setOnClickListener { v: View? ->
            (application as? StarsEarthApplication)?.analyticsManager?.logActionEvent("se1_send_otp_again", Bundle())
            sendOTP(phoneNumber)
            mCountDownTimer!!.start()
            mViewOTPTimer!!.visibility = View.VISIBLE
            mViewSendOTPAgain!!.visibility = View.GONE

            val builder = createAlertDialog()
            builder.setMessage(R.string.otp_sent_again)
                    .setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
                    .show()
        }

        val extras = intent.extras
        phoneNumber = extras!!.getString("phone_number")
        if (mVerificationId == null) {
            mVerificationId = extras!!.getString("verificationId")
        }


        sendOTP(phoneNumber)

        mViewOTPTimer = findViewById<LinearLayout>(R.id.view_otp_timer) as LinearLayout
        mViewSendOTPAgain = findViewById<LinearLayout>(R.id.view_send_otp_again) as LinearLayout
        mViewPleaseWait = findViewById<LinearLayout>(R.id.view_please_wait) as LinearLayout

        startCowntDownTimer()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForLoginPhoneNumberExit("VerifyOTP", "BACK_PRESSED")
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForLoginPhoneNumberExit("VerifyOTP", "HOME_BUTTON_TAPPED")
    }

    private fun sendOTP(phoneNumber: String?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber!!,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
            outState.putString("verificationId", mVerificationId)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationId = savedInstanceState?.getString("verificationId")
    }

    private fun startCowntDownTimer() {
        mTimer = findViewById<TextView>(R.id.tv_timer) as TextView
        mCountDownTimer = object : CountDownTimer(61000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                if (mTimer != null) {
                    if (millisUntilFinished / 1000 < 10) {
                        mTimer!!.setText((millisUntilFinished / 1000 / 60).toString() + ":0" + millisUntilFinished / 1000)
                    } else {
                        val mins = (millisUntilFinished / 1000).toInt() / 60
                        val seconds = (millisUntilFinished / 1000).toInt() % 60
                        mTimer!!.setText(mins.toString() + ":" + if (seconds == 0) "00" else seconds) //If seconds are 0, print double 0, else print seconds
                    }
                }

            }

            override fun onFinish() {
                mViewOTPTimer!!.visibility = View.GONE
                mViewSendOTPAgain!!.visibility = View.VISIBLE
            }
        }
        (mCountDownTimer as CountDownTimer).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer!!.cancel()
    }

    private fun isFormatIncorrect(otp: String): Boolean {
        val builder = createAlertDialog()
        var result = false
        if (otp.length < 1) {
            builder.setMessage(R.string.otp_not_entered)
            result = true
        }

        if (result) {
            builder.setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
            builder.show()
        }


        return result
    }

    private fun createAlertDialog(): AlertDialog.Builder {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(this@SendOTPActivity, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(this@SendOTPActivity)
        }

        return builder
    }

    private fun phoneNumberVerificationSuccessful() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mViewPleaseWait?.visibility = View.VISIBLE
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            updateUserPhoneNumber(user, credential)
            (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForUpdatedPhoneNumber("VerifyOTP")
        }
        else {
            (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForLoginWithPhoneNumber("VerifyOTP")
            signInNewUser(credential)
        }

    }

    private fun updateUserPhoneNumber(user: FirebaseUser, credential: PhoneAuthCredential) {
        user.updatePhoneNumber(credential)
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(task: Task<Void>) {
                        mViewPleaseWait?.visibility = View.GONE
                        if (task.isSuccessful) {
                            Log.d(TAG, "Phone number updated.")
                            phoneNumberVerificationSuccessful()
                        }
                        else {
                            Log.d(TAG, "updatedWithPhoneNumber: failure", task.exception)
                            val builder = createAlertDialog()
                            builder.setMessage((task.exception as Exception).message)
                                    .setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
                                    .show()
                        }
                    }
                })
    }

    private fun linkPhoneNumber(credential: PhoneAuthCredential) {
        mAuth?.currentUser?.linkWithCredential(credential)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "linkWithCredential:success")
                val user = task.result.user
            } else {
                Log.w(TAG, "linkWithCredential:failure", task.exception)
                Toast.makeText(this@SendOTPActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInNewUser(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    mViewPleaseWait?.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        val user = task.result.user
                        phoneNumberVerificationSuccessful()
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            val builder = createAlertDialog()
                            builder.setMessage((task.exception as FirebaseAuthInvalidCredentialsException).message)
                                    .setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
                                    .show()
                        }
                    }
                })
    }
}
