package com.capgemini.reminderapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class MyDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dlg : Dialog

        var bundle = arguments
        val message = bundle?.getString("message")
        val btn1_title = bundle?.getString("button1")
        val btn2_title = bundle?.getString("button2")
        val dlgType = bundle?.getInt("type")

        val parent = activity!!
        val builder = AlertDialog.Builder(parent)

        when(dlgType){
            1 -> {
                // Time Picker
                dlg = TimePickerDialog(parent, parent as TimePickerDialog.OnTimeSetListener,
                    12, 0, false)

            }
            2 -> {
                //Date Picker
                val calendar = Calendar.getInstance()
                dlg = DatePickerDialog(parent, parent as DatePickerDialog.OnDateSetListener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE))
            }
            3 -> {
                //Alert
                builder.setTitle("Confirmation!")
                builder.setMessage(message)
                builder.setPositiveButton(btn1_title, DialogInterface.OnClickListener{
                        dialogInterface, i ->
                    parent.finish()
                })

                builder.setNegativeButton(btn2_title){
                        dlg, i ->
                    dlg.cancel()
                }

                builder.setCancelable(false)

                dlg = builder.create()
            }
        }
        return dlg
    }
}