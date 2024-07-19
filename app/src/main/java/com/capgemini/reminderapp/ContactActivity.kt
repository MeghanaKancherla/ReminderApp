package com.capgemini.reminderapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class ContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
    }

    fun onButtonClick(view: View) {
        when(view.id){
            R.id.smsB -> {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:9898989898"))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }
            R.id.callB -> {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel: 9898989898"))
                startActivity(callIntent)
            }
            R.id.emailB -> {
                val arr = arrayOf("xyz@test.com")
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // only email apps should handle this
                    putExtra(Intent.EXTRA_EMAIL, arr)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }
        }
    }
}