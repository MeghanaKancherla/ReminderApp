package com.capgemini.reminderapp

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_reminder.*
import java.text.SimpleDateFormat
import java.util.*

//val MY_BROADCAST_SCHEDULE_ACTION = "com.capgemini.ReminderApp.action.scheduleAlarm"

class AddReminder : AppCompatActivity(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    //val lReceiver = AlarmReceiver()
    lateinit var pref : SharedPreferences
    lateinit var dataBaseWrapper: DataBaseWrapper

    companion object {
        lateinit var date: String
        lateinit var time: String
        lateinit var task: String
        lateinit var title1: String
        lateinit var alarmManager : AlarmManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

//        val filter1 = IntentFilter(MY_BROADCAST_SCHEDULE_ACTION)
//        registerReceiver(lReceiver, filter1)

        pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        dataBaseWrapper = DataBaseWrapper(this)
    }

    fun onCalenderClick(view: View) {

        val dlg = MyDialog()
        val bundle = Bundle()
        bundle.putInt("type", 2)

        dlg.arguments = bundle
        dlg.show(supportFragmentManager, "Date")
    }
    fun onTimerClick(view: View) {

        val dlg = MyDialog()
        val bundle = Bundle()
        bundle.putInt("type", 1)

        dlg.arguments = bundle
        dlg.show(supportFragmentManager, "Time")
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        var t1 = ""
        if(p2==0){
            t1 = "$p1 : $p2"
            t1 += "0"
        }
        t1 = "$p1 : $p2"
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
        title1 = editTitleE.text.toString()
        task = editTaskE.text.toString()
        time = editTimeE.text.toString()
        date = editDateE.text.toString()

        when{
            title1.isEmpty() -> editTitleE.setError("Enter title")
            task.isEmpty() -> editTaskE.setError("Enter the Description")
            time.isEmpty() -> editTimeE.setError("Select a time")
            date.isEmpty() -> editDateE.setError("Select a date")
            else -> {
                dataBaseWrapper.saveData(title1, task, time, date)
                sendNotification("Information", "Your reminder is added with title $title1")
                val cDate = date.replace('-','/')
                val cTime = time.replace(" ","")
                val fullDate = "$cDate $cTime:00"
                val myDate = fullDate
                val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val changedDate: Date = sdf.parse(myDate)
                val millis: Long = changedDate.getTime()

                setAlarm(millis)
                addEventToCalendar(title1, task, millis)
            }
        }
    }

    @SuppressLint("Range")
    fun setAlarm(millis: Long){

        // Getting the unique id from the database for the added element, then passing it as request code to pending intent
        val getCursor = dataBaseWrapper.retrieveID(title1)
        var id = 0
        if(getCursor.count > 0){
            getCursor.moveToFirst()
            id = getCursor.getInt(getCursor.getColumnIndex(DataBaseHelper.COLUMN_ID))
        }
        //Toast.makeText(this, "Request code: $id", Toast.LENGTH_LONG).show()
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val Btime = pref.getInt("before_time", 0)
        val bIntent = Intent(this, AlarmReceiver::class.java)
        bIntent.putExtra("aTitle", title1)
        bIntent.putExtra("aTask", task)
        val pi = PendingIntent.getBroadcast(this, id, bIntent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(AlarmManager.RTC_WAKEUP, millis-(Btime*1000*60), pi)
        Toast.makeText(this, "Reminder is added", Toast.LENGTH_LONG).show()
    }

    fun onCancelReminder(view: View) {
        val dlg = MyDialog()
        var bundle = Bundle()
        bundle.putString("message", "Do you want to exit?")
        bundle.putString("button1", "Yes")
        bundle.putString("button2", "No")
        bundle.putInt("type", 3)
        dlg.arguments = bundle
        dlg.show(supportFragmentManager, "Cancel")
    }

    fun addEventToCalendar(title: String, description: String, millis: Long){
//        val intent = Intent(Intent.ACTION_INSERT).apply {
//            data = CalendarContract.Events.CONTENT_URI
//            putExtra(CalendarContract.Events.TITLE, title)
//            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
//            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, begin + 1000 * 60 * 60)
//        }
//        startActivity(intent)
        val cr = contentResolver
        val timeZone = TimeZone.getDefault()
        val values = ContentValues()
        values.put(CalendarContract.Events.DTSTART, millis)
        values.put(CalendarContract.Events.DTEND, millis+60*60*1000)
        values.put(CalendarContract.Events.TITLE, title)
        values.put(CalendarContract.Events.DESCRIPTION, description)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.id)
        values.put(CalendarContract.Events.CALENDAR_ID, 1)
        values.put(CalendarContract.Events.HAS_ALARM, 1)

        cr.insert(CalendarContract.Events.CONTENT_URI, values)
        //Toast.makeText(this, "Inserted into calendar", Toast.LENGTH_LONG).show()
    }

    private fun sendNotification(title_notify: String, task_notify: String) {

        val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        lateinit var builder : Notification.Builder

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    "Test", "ReminderApp",
                    NotificationManager.IMPORTANCE_DEFAULT
            )

            nManager.createNotificationChannel(channel)
            builder = Notification.Builder(this, "Test")
        }
        else{
            builder = Notification.Builder(this)
        }

        builder.setSmallIcon(android.R.drawable.ic_popup_reminder)
        builder.setContentTitle(title_notify)
        builder.setContentText(task_notify)
        builder.setAutoCancel(true)

        val intent = Intent(this, ViewReminder::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        builder.setContentIntent(pi)
        val myNotification = builder.build()

        nManager.notify(1, myNotification)
    }

}