package com.starsearth.one.activity.auth

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.starsearth.one.R
import com.starsearth.one.application.StarsEarthApplication
import java.util.concurrent.TimeUnit

class AddEditPhoneNumberActivity : AppCompatActivity() {

    var TAG = "AddEditPhoneNumberActivity"
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private var mAuth: FirebaseAuth? = null
    private var phoneNumber: String? = null

    private var mViewPleaseWait: LinearLayout? = null

    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:" + credential);

            signInWithPhoneAuthCredential(credential);
            //Please wait view will be made invisible in the sign in callback, not here
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
            mViewPleaseWait?.visibility = View.GONE

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
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
        }

        override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
            super.onCodeSent(verificationId, token)
            mViewPleaseWait?.visibility = View.GONE
            val intent = Intent(this@AddEditPhoneNumberActivity, SendOTPActivity::class.java)
            val bundle = Bundle()
            bundle.putString("phone_number", phoneNumber)
            bundle.putString("verificationId", verificationId)
            intent.putExtras(bundle)
            startActivityForResult(intent, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_phone_number)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance()
        val etPhoneNumber = findViewById<EditText>(R.id.et_phone_number) as EditText

        val extras = intent.extras
        if (extras != null) {
            phoneNumber = extras.getString("phone_number")
            etPhoneNumber.setText(phoneNumber)
        }

        val btnSendOTP = findViewById<Button>(R.id.btn_send_otp) as Button
        btnSendOTP.setOnClickListener(View.OnClickListener {
            val availability = GoogleApiAvailability.getInstance()
            val available = availability.isGooglePlayServicesAvailable(applicationContext)
            if (available == ConnectionResult.SUCCESS) {
                var etText = etPhoneNumber.text.toString()
                etText = etText.replace("+0", "")
                etText = etText.replace("+91", "")
                if (!isFormatIncorrect(etText)) {
                    phoneNumber = "+91" + etText
                    val builder = createAlertDialog()
                    builder.setTitle(R.string.correct_number_question)
                            .setMessage(phoneNumber)
                            .setNegativeButton(android.R.string.no) { dialog, which -> dialog.dismiss() }
                            .setPositiveButton(android.R.string.yes) { dialog, which ->
                                sendOTP(phoneNumber)
                                mViewPleaseWait?.visibility = View.VISIBLE
                            }
                            .show()
                }
            } else {
                availability.showErrorDialogFragment(this@AddEditPhoneNumberActivity, available, 1)
            }




        })

        mViewPleaseWait = findViewById<LinearLayout>(R.id.llPleaseWait) as LinearLayout
    }

    override fun onBackPressed() {
        super.onBackPressed()
        (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForLoginPhoneNumberExit("EnterPhoneNumber", "BACK_PRESSED")
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        //home button tapped
        (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForLoginPhoneNumberExit("EnterPhoneNumber", "HOME_BUTTON_TAPPED")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            //If OTP login was successful
            val userId = data?.extras?.getString("userId")
            if (userId != null) {
                phoneNumberVerificationSuccessful(userId)
            }
            else {
                val builder = createAlertDialog()
                builder.setMessage(R.string.error)
                builder.show()
            }
        }
    }

    private fun isFormatIncorrect(phoneNumber: String): Boolean {
        val builder = createAlertDialog()
        var result = false
        if (phoneNumber.length < 1) {
            builder.setMessage(R.string.not_entered_phone_number)
            result = true
        }
        else if (phoneNumber.length != 10) {
            builder.setMessage(R.string.phone_number_10_digits)
            result = true
        }

        if (result) {
            builder.setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
            builder.show()
        }


        return result
    }

    private fun sendOTP(phoneNumber: String?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber!!,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private fun createAlertDialog(): AlertDialog.Builder {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(this@AddEditPhoneNumberActivity, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(this@AddEditPhoneNumberActivity)
        }

        return builder
    }

    private fun phoneNumberVerificationSuccessful(userId : String) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putString("userId", userId)
        intent.putExtras(bundle)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            updateUserPhoneNumber(user, credential)
        }
        else {
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
                            phoneNumberVerificationSuccessful(user.uid)
                            (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForUpdatedPhoneNumber("EditPhoneNumber")
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

    private fun signInNewUser(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    mViewPleaseWait?.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForLoginWithPhoneNumber("EnterPhoneNumber")

                        val user = task.result.user
                        val builder = createAlertDialog()
                        builder.setMessage(R.string.login_successful)
                                .setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
                                .show()
                        phoneNumberVerificationSuccessful(user.uid)
                        // ...
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
