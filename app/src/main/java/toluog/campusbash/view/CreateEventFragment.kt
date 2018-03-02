package toluog.campusbash.view

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.myhexaville.smartimagepicker.ImagePicker
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.FirebaseManager
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.create_event_layout.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.support.v4.act
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import toluog.campusbash.model.Event
import java.lang.ClassCastException
import java.util.*
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import toluog.campusbash.utils.AppContract.Companion.PLACE_AUTOCOMPLETE_REQUEST_CODE
import com.google.android.gms.location.places.Place
import android.app.Activity.RESULT_CANCELED
import kotlinx.android.synthetic.main.pick_event_type_fragment_layout.*
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.progressDialog
import org.jetbrains.anko.support.v4.toast
import toluog.campusbash.model.LatLng
import toluog.campusbash.model.Media
import kotlin.collections.ArrayList

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
    private var mCallback: CreateEventFragmentInterface? = null
    private var type = 0
    private lateinit var viewModel: CreateEventViewModel
    var isSaved = false


    interface CreateEventFragmentInterface {
        fun eventSaved(event: Event)
        fun createTicket()
        fun getTicketList(): ArrayList<Ticket>
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.create_event_layout, container, false)
        fbasemanager = FirebaseManager()
        viewModel = ViewModelProviders.of(activity).get(CreateEventViewModel::class.java)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        updateUi(view?.context)
        if(isSaved) updateUi()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker?.handleActivityResult(resultCode,requestCode, data)

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(activity.applicationContext, data)
                updateLocation(place)
                Log.i(TAG, "Place: " + place.name)
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(activity.applicationContext, data)
                toast("Could not get location")
                Log.i(TAG, status.statusMessage)
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
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
                    viewModel.imageUri = imageUri
                    event_image.setImageURI(imageUri) })

        event_save_button.setOnClickListener { save() }

        event_add_ticket_button.setOnClickListener { mCallback?.createTicket() }

        event_image.setOnClickListener { imagePicker?.choosePicture(true) }

        event_address_layout.setOnClickListener {
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                        .build(activity)
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
            } catch (e: GooglePlayServicesRepairableException) {
                Log.d(TAG, e.message)
            } catch (e: GooglePlayServicesNotAvailableException) {
                Log.d(TAG, e.message)
            }

        }

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
            viewModel.event.startTime = startCalendar.timeInMillis
        }
        else{
            endCalendar.set(Calendar.DAY_OF_YEAR, year)
            endCalendar.set(Calendar.MONTH, month)
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            event_end_date.text = Util.formatDate(endCalendar)
            viewModel.event.endTime = endCalendar.timeInMillis
        }
    }

    fun timeChanged(hourOfDay: Int, minute: Int) {
        if(type == 0){
            startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            startCalendar.set(Calendar.MINUTE, minute)
            event_start_time.text = Util.formatTime(startCalendar)
            viewModel.event.startTime = startCalendar.timeInMillis
        }
        else{
            endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            endCalendar.set(Calendar.MINUTE, minute)
            event_end_time.text = Util.formatTime(endCalendar)
            viewModel.event.endTime = endCalendar.timeInMillis
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

    private fun updateLocation(place: Place) {
        viewModel.event.place.latLng = LatLng(place.latLng.latitude, place.latLng.longitude)
        viewModel.event.place.address = place.address.toString()
        viewModel.event.place.name = place.name.toString()
        viewModel.event.place.id = place.id
        if(place.name.isNotEmpty() && place.address.isNotEmpty()) {
            address_text.text = "${place.name} | ${place.address}"
        }
    }

    private fun save() {
        val title = event_name.text.toString().trim()
        val description = event_description.text.toString().trim()
        val eventType = adapter.getItem(event_type_spinner.selectedItemPosition).toString()
        val startTime = startCalendar.timeInMillis
        val endTime = endCalendar.timeInMillis
        val uri = imageUri
        val tickets = mCallback?.getTicketList() ?: ArrayList<Ticket>()
        if (tickets.size == 0) {
            toast("You must add one ticket")
            return
        }
        Log.d(TAG, "$tickets")
        val university = Util.getPrefString(act, AppContract.PREF_UNIVERSITY_KEY)

        if (TextUtils.isEmpty(title)) {
            event_name.error = "Please enter name"
            return
        }

        val event = viewModel.event
        event.eventName = title
        event.eventType = eventType
        event.description = description
        event.university = university
        event.startTime = startTime
        event.endTime = endTime
        event.creator = AppContract.CREATOR
        event.tickets = tickets
        event.timeZone = Calendar.getInstance().timeZone.displayName
        Log.d(TAG, "${event.tickets}")

        val creator = FirebaseManager.getCreator()
        if (creator != null) event.creator = creator

        val dialog = indeterminateProgressDialog(message = "", title = "Uploading Event")
        dialog.show()

        if (uri != null) {
            Log.d(TAG, "uri is not null")
            dialog.setMessage("Uploading media")
            fbasemanager.uploadEventImage(uri)?.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                val placeholder = taskSnapshot?.storage?.path?.let{
                    Media(taskSnapshot.downloadUrl.toString(), it, "image")
                }
                event.placeholderImage = placeholder
                fbasemanager.addEvent(event)
                snackbar(rootView!!, "Event saved")
                mCallback?.eventSaved(event)

            }
        }
        else{
            Log.d(TAG, "uri is null")
            fbasemanager.addEvent(event)
            snackbar(rootView!!, "Event saved")
            mCallback?.eventSaved(event)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try{
            mCallback = context as CreateEventFragmentInterface
        }
        catch (e: ClassCastException){
            Log.d(TAG, e.message)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "OnSavedInstanceState")
    }

    private fun updateUi(){
        val event = viewModel.event
        val sTime = Calendar.getInstance()
        val endTime = Calendar.getInstance()
        sTime.timeInMillis = event.startTime
        endTime.timeInMillis = event.endTime
        event_name.setText(event.eventName)
        event_type_spinner.setSelection(adapter.getPosition(event.eventType))
        event_start_date.text = Util.formatDate(sTime)
        event_start_time.text = Util.formatTime(sTime)
        event_end_date.text = Util.formatDate(endTime)
        event_end_time.text = Util.formatTime(endTime)
        val place = viewModel.event.place
        if(place.name.isNotEmpty() && place.address.isNotEmpty()) {
            address_text.text = "${place.name} | ${place.address}"
        }
        if(viewModel.imageUri != null){
            imageUri = viewModel.imageUri
            event_image.setImageURI(imageUri)
        }
    }
}