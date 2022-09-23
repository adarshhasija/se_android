package com.starsearth.two.activity.profile

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.starsearth.two.R
import com.starsearth.two.activity.auth.AddEditPhoneNumberActivity

class PhoneNumberActivity : AppCompatActivity() {

    private var tvPhoneNumber: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number)

        tvPhoneNumber = findViewById<TextView>(R.id.tv_phone_number) as TextView
        val btnChange = findViewById<Button>(R.id.btn_change_phone_number) as Button
        btnChange.setOnClickListener { v: View? ->
            val intent = Intent(this@PhoneNumberActivity, AddEditPhoneNumberActivity::class.java)
            startActivityForResult(intent, 0)
        }


        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            if (!user.phoneNumber.isNullOrBlank()) {
                tvPhoneNumber!!.text = user.phoneNumber
            }
            else {
                tvPhoneNumber!!.text = "---"
                btnChange.text = "Add Phone Number"
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                tvPhoneNumber?.text = user.phoneNumber
                Toast.makeText(applicationContext, R.string.phone_number_updated, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createAlertDialog(): AlertDialog.Builder {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(this@PhoneNumberActivity, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(this@PhoneNumberActivity)
        }

        return builder
    }
}
