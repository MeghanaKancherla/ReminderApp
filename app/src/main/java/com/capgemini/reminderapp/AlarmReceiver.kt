package com.capgemini.reminderapp

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, p1: Intent?) {

        val alarmTitle = p1?.extras?.getString("aTitle")!!
        val alarmTask = p1?.extras?.getString("aTask")!!

        Toast.makeText(context, "Reminder of task $alarmTitle", Toast.LENGTH_LONG).show()

        thread{
            val player = MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI)
            player.start()
            Thread.sleep(5000)
            player.stop()
        }
        sendNotification(alarmTitle, alarmTask, context)
    }

    private fun sendNotification(title_notify: String, task_notify: String, context: Context?) {

        val nManager = context?.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        lateinit var builder : Notification.Builder

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    "Test", "ReminderApp",
                    NotificationManager.IMPORTANCE_DEFAULT
            )

            nManager.createNotificationChannel(channel)
            builder = Notification.Builder(context, "Test")
        }
        else{
            builder = Notification.Builder(context)
        }

        builder.setSmallIcon(android.R.drawable.ic_popup_reminder)
        builder.setContentTitle(title_notify)
        builder.setContentText(task_notify)
        builder.setAutoCancel(true)

        val intent = Intent(context, ViewReminder::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pi)
        val myNotification = builder.build()

        nManager.notify(1, myNotification)
    }

}