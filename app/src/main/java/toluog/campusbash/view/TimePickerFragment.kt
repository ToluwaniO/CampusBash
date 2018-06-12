package toluog.campusbash.view

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.format.DateFormat
import android.widget.TimePicker
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import java.util.*
import toluog.campusbash.view.TimePickerFragment.TimeSetListener




/**
 * Created by oguns on 12/13/2017.
 */
class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener{

    private var mCallback: TimeSetListener? = null
    private val TAG = TimePickerFragment::class.java.simpleName

    interface TimeSetListener{
        fun timeChanged(hourOfDay: Int, minute: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mCallback?.timeChanged(hourOfDay, minute)
    }

    fun setOnTimeSetListener(listener: TimeSetListener){
        mCallback = listener
    }
}