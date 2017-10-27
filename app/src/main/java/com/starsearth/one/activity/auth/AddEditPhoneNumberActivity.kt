package com.starsearth.one.activity.auth

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.starsearth.one.R
import com.starsearth.one.SendOTPActivity

class AddEditPhoneNumberActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_phone_number)

        val etPhoneNumber = findViewById(R.id.et_phone_number) as EditText

        val extras = intent.extras
        if (extras != null) {
            val phoneNumber = extras.getString("phone_number")
            etPhoneNumber.setText(phoneNumber)
        }

        val btnSendOTP = findViewById(R.id.btn_send_otp) as Button
        btnSendOTP.setOnClickListener(View.OnClickListener {

            val phoneNumber = etPhoneNumber.text.toString()
            if (!isFormatIncorrect(phoneNumber)) {
                val finalPhoneNumber = "+91" + phoneNumber
                val builder = createAlertDialog()
                builder.setTitle(R.string.correct_number_question)
                        .setMessage(finalPhoneNumber)
                        .setNegativeButton(android.R.string.no) { dialog, which -> dialog.dismiss() }
                        .setPositiveButton(android.R.string.yes) { dialog, which ->
                            val intent = Intent(this, SendOTPActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("phone_number", finalPhoneNumber)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }
                        .show()
            }


        })
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

    private fun createAlertDialog(): AlertDialog.Builder {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(this@AddEditPhoneNumberActivity, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(this@AddEditPhoneNumberActivity)
        }

        return builder
    }
}
