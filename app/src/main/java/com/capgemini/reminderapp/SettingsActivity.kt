package com.capgemini.reminderapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

val PREF_NAME = "setTime"

class SettingsActivity : AppCompatActivity() {

    var beforeTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun timeSelected(view: View) {
        when (view.id) {
            R.id.fiveR -> {
                beforeTime = 5
            }
            R.id.tenR -> {
                beforeTime = 10
            }
            R.id.fifteenR -> {
                beforeTime = 15
            }
            R.id.thirtyR -> {
                beforeTime = 30
            }
            R.id.oneHR -> {
                beforeTime = 60
            }
            R.id.defaultR -> {
                beforeTime = 0
            }
        }
    }

    fun okSelected(view: View) {

        when (view.id) {
            R.id.selectB -> {
                saveTime(beforeTime)
            }
            R.id.cancelB -> {
                beforeTime = 0
                finish()
            }
        }
    }

    private fun saveTime(time: Int) {
        val pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt("before_time", time)
        editor.commit()

        Toast.makeText(this, "Time is set!!", Toast.LENGTH_LONG).show()
    }

}