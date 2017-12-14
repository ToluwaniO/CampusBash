package toluog.campusbash.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.create_event_layout.*
import toluog.campusbash.R
import toluog.campusbash.Utils.Util
import java.util.*
import kotlin.math.min

/**
 * Created by oguns on 12/13/2017.
 */
class CreateEventFragment : Fragment(){

    private var rootView: View? = null
    private val calendar = Calendar.getInstance()
    private var type = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.create_event_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        updateUi(view?.context)
    }

    private fun updateUi(context: Context?){
        val adapter = ArrayAdapter.createFromResource(context,
                R.array.party_types, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        event_type_spinner.adapter = adapter

        event_save_button.setOnClickListener {  }

        event_start_date.setOnClickListener {
            type = 0
            callDateDialog()
        }

        event_start_time.setOnClickListener {
            type = 0
            callTimeDialog()
        }

        event_end_date.setOnClickListener {
            type = 1
            callDateDialog()
        }

        event_end_time.setOnClickListener {
            type = 1
            callTimeDialog()
        }
    }

    fun dateChanged(year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.DAY_OF_YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        if(type == 0){
            event_start_date.text = Util.formatDate(calendar)
        }
        else{
            event_end_date.text = Util.formatDate(calendar)
        }
    }

    fun timeChanged(hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)

        if(type == 0){
            event_start_time.text = Util.formatTime(calendar)
        }
        else{
            event_end_time.text = Util.formatTime(calendar)
        }
    }

    private fun callDateDialog(){
        val dialog = DatePickerFragment()
        dialog.setOnDateSetListener(object : DatePickerFragment.DateSetListener{
            override fun dateChanged(year: Int, month: Int, dayOfMonth: Int) {
                this@CreateEventFragment.dateChanged(year, month, dayOfMonth)
            }
        })
        dialog.show(activity.supportFragmentManager, null)
    }

    private fun callTimeDialog(){
        val dialog = TimePickerFragment()
        dialog.setOnTimeSetListener(object : TimePickerFragment.TimeSetListener{
            override fun timeChanged(hourOfDay: Int, minute: Int) {
                this@CreateEventFragment.timeChanged(hourOfDay, minute)
            }

        })
        dialog.show(activity.supportFragmentManager, null)
    }
}