package toluog.campusbash.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.firebase.storage.UploadTask
import com.myhexaville.smartimagepicker.ImagePicker
import kotlinx.android.synthetic.main.create_event_layout.*
import org.jetbrains.anko.design.snackbar
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import java.lang.ClassCastException
import java.util.*

/**
 * Created by oguns on 12/13/2017.
 */
class CreateEventFragment : Fragment(){

    private val TAG = CreateEventFragment::class.java.simpleName
    private var rootView: View? = null
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private var imagePicker: ImagePicker? = null
    private lateinit var adapter: ArrayAdapter<CharSequence>
    private var imageUri: Uri? = null
    private lateinit var fbasemanager: FirebaseManager
    private var mCallback: SaveComplete? = null
    private var type = 0

    interface SaveComplete {
        fun eventSaved()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.create_event_layout, container, false)
        fbasemanager = FirebaseManager()
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        updateUi(view?.context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker?.handleActivityResult(resultCode,requestCode, data);
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePicker?.handlePermission(requestCode, grantResults)
    }

    private fun updateUi(context: Context?){
        adapter = ArrayAdapter.createFromResource(context,
                R.array.party_types, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        event_type_spinner.adapter = adapter

        imagePicker = ImagePicker(activity /*activity non null*/,
                this@CreateEventFragment /*fragment nullable*/,
                { /*on image picked*/ imageUri ->
                    this.imageUri = imageUri
                    event_image.setImageURI(imageUri) })

        event_save_button.setOnClickListener { save() }

        event_image.setOnClickListener { imagePicker?.choosePicture(true) }

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
        if(type == 0){
            startCalendar.set(Calendar.DAY_OF_YEAR, year)
            startCalendar.set(Calendar.MONTH, month)
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            event_start_date.text = Util.formatDate(startCalendar)
        }
        else{
            endCalendar.set(Calendar.DAY_OF_YEAR, year)
            endCalendar.set(Calendar.MONTH, month)
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            event_end_date.text = Util.formatDate(endCalendar)
        }
    }

    fun timeChanged(hourOfDay: Int, minute: Int) {

        if(type == 0){
            startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            startCalendar.set(Calendar.MINUTE, minute)
            event_start_time.text = Util.formatTime(startCalendar)
        }
        else{
            endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            endCalendar.set(Calendar.MINUTE, minute)
            event_end_time.text = Util.formatTime(endCalendar)
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

    private fun save() {
        val title = event_name.text.toString()
        val eventType = adapter.getItem(event_type_spinner.selectedItemPosition).toString()
        val startTime = startCalendar.timeInMillis
        val endTime = endCalendar.timeInMillis
        val uri = imageUri
        val tickets = arrayListOf<Ticket>(Ticket("VIP", "Want best service? You're at the right place",
                1, 10, 15.50, 0, startTime, 0, endTime))

        if (TextUtils.isEmpty(title)) {
            event_name.error = "Please enter name"
            return
        }

        val event = Event("", title, eventType, AppContract.LOREM_IPSUM, null,
                null, "uOttawa", AppContract.STANTON_ADDRESS, AppContract.STANTON_COORD,
                startTime, endTime, null, tickets, AppContract.CREATOR)

        if (uri != null) {
            Log.d(TAG, "uri is not null")
            fbasemanager.uploadEventImage(uri)?.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                event.placeholderUrl = taskSnapshot?.downloadUrl.toString()
                fbasemanager.addEvent(event)
                snackbar(rootView!!, "Event saved")
                mCallback?.eventSaved()

            }
        }
        else{
            Log.d(TAG, "uri is null")
            fbasemanager.addEvent(event)
            snackbar(rootView!!, "Event saved")
            mCallback?.eventSaved()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try{
            mCallback = context as SaveComplete
        }
        catch (e: ClassCastException){
            Log.d(TAG, e.message)
        }
    }
}