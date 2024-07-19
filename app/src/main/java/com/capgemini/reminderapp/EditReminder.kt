package com.capgemini.reminderapp

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_reminder.*
import java.text.SimpleDateFormat
import java.util.*

class EditReminder : AppCompatActivity(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    lateinit var date : String
    lateinit var time : String
    lateinit var title : String
    lateinit var task : String
    lateinit var initial_title : String
    lateinit var pref : SharedPreferences
    lateinit var dataBaseWrapper: DataBaseWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reusing the add_reminder activity
        setContentView(R.layout.activity_add_reminder)

        pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        dataBaseWrapper = DataBaseWrapper(this)

        headingT.text = "EDIT REMINDER"
        editReminderB.text = "EDIT"
        val gotBundle = intent?.extras?.getBundle("view_bundle")
        initial_title = gotBundle?.getString("view_title").toString()
        editTitleE.setText("${gotBundle?.getString("view_title")}")
        editTaskE.setText("${gotBundle?.getString("view_task")}")
        editTimeE.setText("${gotBundle?.getString("view_time")}")
        editDateE.setText("${gotBundle?.getString("view_date")}")
    }

    fun onCalenderClick(view: View) {

        val dlg = MyDialog()
        val bundle = Bundle()
        bundle.putInt("type",2)

        dlg.arguments = bundle
        dlg.show(supportFragmentManager, "Date")
    }
    fun onTimerClick(view: View) {

        val dlg = MyDialog()
        val bundle = Bundle()
        bundle.putInt("type",1)

        dlg.arguments = bundle
        dlg.show(supportFragmentManager, "Time")
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        val t1 = "$p1 : $p2"
        editTimeE.setText(t1)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val month = p2+1
        if(month < 10){
            val d1 = "$p1-0$month-$p3"
            editDateE.setText(d1)
        }
        else
        {
            val d1 = "$p1-$month-$p3"
            editDateE.setText(d1)
        }
    }

    fun onAddReminder(view: View) {
        title = editTitleE.text.toString()
        task = editTaskE.text.toString()
        time = editTimeE.text.toString()
        date = editDateE.text.toString()

        when{
            title.isEmpty() -> editTitleE.setError("Enter title")
            task.isEmpty() -> editTaskE.setError("Enter the Description")
            time.isEmpty() -> editTimeE.setError("Select a time")
            date.isEmpty() -> editDateE.setError("Select a date")
            else -> {
                //rList[editIndex] = ReminderList(title, task, time, date)
                sendNotification()
                val cDate = date.replace('-','/')
                val cTime = time.replace(" ","")
                val fullDate = "$cDate $cTime:00"
                Toast.makeText(this,"$fullDate", Toast.LENGTH_LONG).show()
                val myDate = fullDate
                val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val changedDate: Date = sdf.parse(myDate)
                val millis: Long = changedDate.getTime()
                addEventToCalendar(title, task, millis, initial_title)
                updatePendingIntent(millis)
                var bundle = Bundle()
                bundle.putString("edit_title", title)
                bundle.putString("edit_task", task)
                bundle.putString("edit_time", time)
                bundle.putString("edit_date", date)
                val i = intent.putExtra("sent_bundle", bundle)
                setResult(RESULT_OK, i)
                finish()
            }
        }
    }

    @SuppressLint("Range")
    fun updatePendingIntent(millis: Long){

        // fetching the id - which is the request code of the pending intent
        // throught this, we can fetch and update the pending intent
        val getCursor = dataBaseWrapper.retrieveID(title)
        var id = 0
        if(getCursor.count > 0){
            getCursor.moveToFirst()
            id = getCursor.getInt(getCursor.getColumnIndex(DataBaseHelper.COLUMN_ID))
        }
        //Toast.makeText(this, "Request code: $id", Toast.LENGTH_LONG).show()
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val Btime = pref.getInt("before_time", 0)
        val bIntent = Intent(this, AlarmReceiver::class.java)
        bIntent.putExtra("aTitle", title)
        bIntent.putExtra("aTask", task)
        val pi = PendingIntent.getBroadcast(this, id, bIntent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(AlarmManager.RTC_WAKEUP, millis-(Btime*1000*60), pi)
        Toast.makeText(this, "Reminder is editted", Toast.LENGTH_LONG).show()
    }

    fun onCancelReminder(view: View) {
        val dlg = MyDialog()
        var bundle = Bundle()
        bundle.putString("message", "Do you want to exit?")
        bundle.putString("button1", "Yes")
        bundle.putString("button2", "No")
        bundle.putInt("type",3)
        dlg.arguments = bundle
        dlg.show(supportFragmentManager, "Cancel")
    }

    private fun sendNotification() {

        val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        lateinit var builder : Notification.Builder

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Test", "ReminderApp",
                NotificationManager.IMPORTANCE_DEFAULT)

            nManager.createNotificationChannel(channel)
            builder = Notification.Builder(this, "Test")
        }
        else{
            builder = Notification.Builder(this)
        }

        builder.setSmallIcon(android.R.drawable.ic_popup_reminder)
        builder.setContentTitle("Information")
        builder.setContentText("Your Reminder is Edited with\ntitle $title")
        builder.setAutoCancel(true)

        val intent = Intent(this, ViewReminder::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pi)
        val myNotification = builder.build()

        nManager.notify(1, myNotification)
    }


    fun addEventToCalendar(title: String, description: String, millis: Long, beforeTitle: String){
        val cr = contentResolver
        val whereClause = "${CalendarContract.Events.TITLE} = ?"
        val whereArgs = arrayOf(beforeTitle)
        val timeZone = TimeZone.getDefault()
        val values = ContentValues()
        values.put(CalendarContract.Events.DTSTART, millis)
        values.put(CalendarContract.Events.DTEND, millis+60*60*1000)
        values.put(CalendarContract.Events.TITLE, title)
        values.put(CalendarContract.Events.DESCRIPTION, description)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.id)
        values.put(CalendarContract.Events.CALENDAR_ID, 1)
        values.put(CalendarContract.Events.HAS_ALARM, 1)

        cr.update(CalendarContract.Events.CONTENT_URI, values, whereClause, whereArgs)
        Toast.makeText(this, "Inserted into calendar", Toast.LENGTH_LONG).show()
    }
}