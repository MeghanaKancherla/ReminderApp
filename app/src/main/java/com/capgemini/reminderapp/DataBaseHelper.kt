package com.capgemini.reminderapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DataBaseHelper(context: Context) : SQLiteOpenHelper (context, "Reminders.db", null, 1){

    companion object{
        val TABLE_NAME = "REMINDER"
        val COLUMN_TITLE = "title"
        val COLUMN_TASK = "task"
        val COLUMN_TIME = "time"
        val COLUMN_DATE = "date"
        val COLUMN_ID = "_id"
        val TABLE_QUERY = "create table $TABLE_NAME ($COLUMN_ID integer PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE text, $COLUMN_TASK text, $COLUMN_TIME text, $COLUMN_DATE text)"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        try{
            db?.execSQL(TABLE_QUERY)
        }catch(e: Exception){
            Log.e("DBHelper", "ERROR creating table: ${e.message}")
        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }
}