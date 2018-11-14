package toluog.campusbash.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.util.Log
import android.widget.DatePicker
import java.lang.ClassCastException
import java.util.*

/**
 * Created by oguns on 12/13/2017.
 */
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener{

    private var mCallback: DateSetListener? = null
    private val TAG = DatePickerFragment::class.java.simpleName

    interface DateSetListener {
        fun dateChanged(year: Int, month: Int, dayOfMonth: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mCallback?.dateChanged(year, month, dayOfMonth)
    }

    fun setOnDateSetListener(listener: DateSetListener){
        mCallback = listener
    }
}