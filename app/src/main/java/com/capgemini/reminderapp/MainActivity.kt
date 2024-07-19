package com.capgemini.reminderapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onAddReminder(view: View) {
        val i = Intent(this, AddReminder::class.java)
        startActivity(i)
    }
    fun onViewReminder(view: View) {
        val i1 = Intent(this, ViewReminder::class.java)
        startActivity(i1)
    }

    val MENU_CONTACT = 1
    val MENU_SETTINGS = 2
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, MENU_CONTACT, 0, "CONTACT US")
        menu?.add(0, MENU_SETTINGS, 1, "SETTINGS")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            MENU_CONTACT ->{
                val i = Intent(this, ContactActivity::class.java)
                startActivity(i)
            }
            MENU_SETTINGS -> {
                val i1 = Intent(this, SettingsActivity::class.java)
                startActivity(i1)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}