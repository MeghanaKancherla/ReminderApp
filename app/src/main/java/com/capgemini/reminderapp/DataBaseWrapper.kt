package com.capgemini.reminderapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class DataBaseWrapper(val context: Context) {

    val helper = DataBaseHelper(context)
    val db : SQLiteDatabase = helper.writableDatabase

    fun saveData(title: String, task: String, time: String, date: String) : Long{
        val values = ContentValues()
        values.put(DataBaseHelper.COLUMN_TITLE, title)
        values.put(DataBaseHelper.COLUMN_TASK, task)
        values.put(DataBaseHelper.COLUMN_TIME, time)
        values.put(DataBaseHelper.COLUMN_DATE, date)
        return db.insert(DataBaseHelper.TABLE_NAME, null, values)
    }

    fun retrieveValues() : Cursor {
        val columns = arrayOf(DataBaseHelper.COLUMN_ID, DataBaseHelper.COLUMN_TITLE, DataBaseHelper.COLUMN_TASK, DataBaseHelper.COLUMN_TIME, DataBaseHelper.COLUMN_DATE)
        return db.query(DataBaseHelper.TABLE_NAME, columns, null, null, null, null, null)
    }

    fun deleteRow(reminder: ReminderList){
        val whereClause = "${DataBaseHelper.COLUMN_TITLE} = ?"
        val whereArgs = arrayOf(reminder.title)
        db.delete(DataBaseHelper.TABLE_NAME, whereClause, whereArgs)
    }

    fun updateRow(reminder: ReminderList, title: String){
        val values = ContentValues()
        values.put(DataBaseHelper.COLUMN_TITLE, reminder.title)
        values.put(DataBaseHelper.COLUMN_TASK, reminder.task)
        values.put(DataBaseHelper.COLUMN_TIME, reminder.time)
        values.put(DataBaseHelper.COLUMN_DATE, reminder.date)
        val whereClause = "${DataBaseHelper.COLUMN_TITLE} = ?"
        val whereArgs = arrayOf(title)
        db.update(DataBaseHelper.TABLE_NAME, values, whereClause, whereArgs)
    }

    fun retrieveID(title: String) : Cursor{
        val columns = arrayOf(DataBaseHelper.COLUMN_ID)
        val selection = "${DataBaseHelper.COLUMN_TITLE} = ?"
        val selectionArgs = arrayOf(title)
        return db.query(DataBaseHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null)
    }
}