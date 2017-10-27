package com.starsearth.one

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SendOTPActivity : AppCompatActivity() {

    private val phoneNumber: String? = null

    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            var i = 0
        }

        override fun onVerificationFailed(e: FirebaseException) {
            var i = 0
        }

        override fun onCodeSent(p0: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
            super.onCodeSent(p0, p1)
            var i = 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_otp)

        val etOTP = findViewById(R.id.et_otp) as EditText

        val btnSubmit = findViewById(R.id.btn_submit) as Button
        btnSubmit.setOnClickListener { v: View? ->
            var otp = etOTP.text.toString()
            if (!isFormatIncorrect(otp)) {

            }
        }

        val btnSendOTPAgain = findViewById(R.id.btn_send_otp_again) as Button
        btnSendOTPAgain.setOnClickListener { v: View? ->

        }

        val extras = intent.extras
        val phoneNumber = extras!!.getString("phone_number")

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private fun isFormatIncorrect(phoneNumber: String): Boolean {
        val builder = createAlertDialog()
        var result = false
        if (phoneNumber.length < 1) {
            builder.setMessage(R.string.otp_not_entered)
            result = true
        }
        else if (phoneNumber.length > 4) {
            builder.setMessage(R.string.otp_only_4_digits)
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
}
