package com.capgemini.reminderapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_view_reminder.*

class ViewReminder : AppCompatActivity(), AdapterView.OnItemClickListener {

    var reminderList = mutableListOf<ReminderList>()
    lateinit var adapter : ArrayAdapter<ReminderList>
    lateinit var cursorAdapter : CursorAdapter
    lateinit var dataBaseWrapper: DataBaseWrapper
    var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_reminder)

        dataBaseWrapper = DataBaseWrapper(this)
        displayReminders()
        val cursor1 = dataBaseWrapper.retrieveValues()
        val from = arrayOf<String>(DataBaseHelper.COLUMN_TITLE, DataBaseHelper.COLUMN_TASK, DataBaseHelper.COLUMN_TIME)
        val to = intArrayOf(R.id.titleT, R.id.taskT, R.id.timeT)
        cursorAdapter = ReminderAdapter1(this, cursor1)
        listView.adapter = cursorAdapter
        adapter = ReminderAdapter(this, R.layout.view_reminder_list, reminderList)
        //listView.adapter = adapter
        listView.setOnItemClickListener(this)
    }

    fun displayReminders() {
        val cursor = dataBaseWrapper.retrieveValues()
        if(cursor.count>0){
            cursor.moveToFirst()
            do{
                val title = cursor.getString(cursor.getColumnIndex(DataBaseHelper.COLUMN_TITLE))
                val task = cursor.getString(cursor.getColumnIndex(DataBaseHelper.COLUMN_TASK))
                val time = cursor.getString(cursor.getColumnIndex(DataBaseHelper.COLUMN_TIME))
                val date = cursor.getString(cursor.getColumnIndex(DataBaseHelper.COLUMN_DATE))
                reminderList.add(ReminderList(title, task, time, date))
            }while(cursor.moveToNext())
        }
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, index1: Int, p3: Long) {
        val MENU_DELETE = 1
        val MENU_EDIT = 2
        val popMenu = PopupMenu(this, listView)
        val menu = popMenu.menu
        menu.add(0, MENU_DELETE, 0, "DELETE")
        menu.add(0, MENU_EDIT, 1, "EDIT")
        popMenu.show()

        index = index1
        popMenu.setOnMenuItemClickListener {
            val selectedItem = reminderList[index]
            when(it.itemId){
                MENU_DELETE -> {
                    Toast.makeText(this, "Delete Selected!", Toast.LENGTH_LONG).show()
                    reminderList.removeAt(index)
                    dataBaseWrapper.deleteRow(selectedItem)
                    //adapter.notifyDataSetChanged()
                    cursorAdapter.changeCursor(dataBaseWrapper.retrieveValues())
                    cursorAdapter.notifyDataSetChanged()
                    val dTitle = selectedItem.title
                    deleteFromCalendar(dTitle)
                    val getCursor = dataBaseWrapper.retrieveID(selectedItem.title)
                    var id = 0
                    if(getCursor.count > 0){
                        getCursor.moveToFirst()
                        id = getCursor.getInt(getCursor.getColumnIndex(DataBaseHelper.COLUMN_ID))
                    }
                    deletePendingIntent(id)
                    true
                }
                else -> {
                    Toast.makeText(this, "Edit Selected!", Toast.LENGTH_LONG).show()
                    val i1 = Intent(this, EditReminder::class.java)
                    val rem = reminderList[index]
                    var bundle = Bundle()
                    bundle.putString("view_title", rem.title)
                    bundle.putString("view_task", rem.task)
                    bundle.putString("view_time", rem.time)
                    bundle.putString("view_date", rem.date)
                    i1.putExtra("view_bundle", bundle)
                    startActivityForResult(i1, 1)
                    //adapter.notifyDataSetChanged()
                    true
                }
            }
        }
    }

    fun deletePendingIntent(id : Int){
        val bIntent = Intent(this, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this, id, bIntent, 0)
        pi.cancel()
    }

    fun deleteFromCalendar(title: String){
        val cr = contentResolver
        val whereClause = "${CalendarContract.Events.TITLE} = ?"
        val whereArgs = arrayOf(title)
        cr.delete(CalendarContract.Events.CONTENT_URI, whereClause, whereArgs)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            1 -> {
                if(resultCode == RESULT_OK){
                    val eTitle = reminderList[index].title
                    val receivedBundle = data?.extras?.getBundle("sent_bundle")
                    val editted = ReminderList(receivedBundle?.getString("edit_title")!!, receivedBundle.getString("edit_task")!!, receivedBundle.getString("edit_time")!!, receivedBundle.getString("edit_date")!!)
                    reminderList[index] = editted
                    dataBaseWrapper.updateRow(editted, eTitle)
                    cursorAdapter.changeCursor(dataBaseWrapper.retrieveValues())
                    cursorAdapter.notifyDataSetChanged()
                    //adapter.notifyDataSetChanged()
                }
                else if(resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "Nothing was edited!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

// Cursor Adapter
class ReminderAdapter1(val context: Context, cursor: Cursor) : CursorAdapter(context, cursor) {

    companion object{
        var rowCount = 0
    }
    override fun newView(p0: Context?, p1: Cursor?, p2: ViewGroup?): View {
        return  LayoutInflater.from(context).inflate(R.layout.view_reminder_list, p2, false)
    }

    override fun bindView(view: View?, p1: Context?, cursor: Cursor?) {

        val rTitle = view?.findViewById<TextView>(R.id.titleT)
        val rTask = view?.findViewById<TextView>(R.id.taskT)
        val rTime = view?.findViewById<TextView>(R.id.timeT)
        rTitle?.text = cursor?.getString(cursor.getColumnIndex(DataBaseHelper.COLUMN_TITLE))
        rTask?.text = cursor?.getString(cursor.getColumnIndex(DataBaseHelper.COLUMN_TASK))
        rTime?.text = "Set on ${cursor?.getString(cursor.getColumnIndex(DataBaseHelper.COLUMN_DATE))} at ${cursor?.getString(cursor.getColumnIndex(DataBaseHelper.COLUMN_TIME))}"
        rowCount++
        if(rowCount % 2 == 0){
            view?.setBackgroundColor(Color.parseColor("#F8E0E2"))
        }
        else{
            view?.setBackgroundColor(Color.parseColor("#DAF6FA"))
        }
    }
}

// Array Adapter
class ReminderAdapter(context: Context, val layoutRes: Int, val data: List<ReminderList>) : ArrayAdapter<ReminderList>(context, layoutRes, data){

    override fun getItem(position: Int): ReminderList? {
        return data[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val reminder = getItem(position)
        var view = convertView ?: LayoutInflater.from(context).inflate(layoutRes, null)
        val rTitle = view.findViewById<TextView>(R.id.titleT)
        val rTask = view.findViewById<TextView>(R.id.taskT)
        val rTime = view.findViewById<TextView>(R.id.timeT)
        rTitle.text = reminder?.title
        rTask.text = reminder?.task
        rTime.text = "Set on ${reminder?.date} at ${reminder?.time}"
        if(position % 2 == 0){
            view?.setBackgroundColor(Color.parseColor("#F8E0E2"))
        }
        else{
            view?.setBackgroundColor(Color.parseColor("#DAF6FA"))
        }

        return view
    }

}