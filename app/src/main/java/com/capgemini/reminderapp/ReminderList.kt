package com.capgemini.reminderapp

data class ReminderList(var title: String, var task: String, var time:String, var date: String/*, var pi: PendingIntent*/) {

    override fun toString(): String {
        return "Title : $title\nTask : $task\nSet at $time on $date"
    }
}