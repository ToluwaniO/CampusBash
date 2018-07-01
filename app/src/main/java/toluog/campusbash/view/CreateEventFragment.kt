package toluog.campusbash.view

import android.os.Bundle
import android.view.View
import com.myhexaville.smartimagepicker.ImagePicker
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.create_event_layout.*
import org.jetbrains.anko.support.v4.act
import toluog.campusbash.R
import java.lang.ClassCastException
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import toluog.campusbash.utils.AppContract.Companion.PLACE_AUTOCOMPLETE_REQUEST_CODE
import com.google.android.gms.location.places.Place
import android.app.Activity.RESULT_CANCELED
import android.app.ProgressDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.widget.ImageView
import com.crashlytics.android.Crashlytics
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast
import toluog.campusbash.model.*
import toluog.campusbash.utils.*
import java.util.Calendar
import kotlin.collections.ArrayList

/**
 * Created by oguns on 12/13/2017.
 */
class CreateEventFragment : Fragment(){

    private val TAG = CreateEventFragment::class.java.simpleName
    private lateinit var rootView: View
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private var imagePicker: ImagePicker? = null
    private lateinit var fbasemanager: FirebaseManager
    private var mCallback: CreateEventFragmentInterface? = null
    private var type = 0
    private lateinit var viewModel: CreateEventViewModel
    private lateinit var country: String
    private lateinit var countries: List<String>
    private val universities = ArrayList<String>()
    var isSaved = false
    private val user = FirebaseManager.getUser()
    private val creator = Creator()


    interface CreateEventFragmentInterface {
        fun eventSaved(event: Event)
        fun createTicket()
        fun getTicketList(): ArrayList<Ticket>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.create_event_layout, container, false)
        fbasemanager = FirebaseManager()
        viewModel = ViewModelProviders.of(activity!!).get(CreateEventViewModel::class.java)
        country = Util.getPrefString(activity!!, AppContract.PREF_COUNTRY_KEY)
        viewModel.event.university = Util.getPrefString(activity!!, AppContract.PREF_UNIVERSITY_KEY)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bundle = arguments
        countries = resources.getStringArray(R.array.countries).asList()

        viewModel.getUniversities(country)?.observe(this, Observer {
            universities.clear()
            it?.forEach {university ->
                universities.add(university.name)
            }
        })

        if(user != null) {
            viewModel.getUser(user.uid).observe(activity!!, Observer {
                if(it != null) {
                    Log.d(TAG, "user -> $it")
                    updateCreator(it)
                }
            })
        }
        updateUi(view.context)
        if(isSaved) updateUi()
        else if(bundle != null) {
            val event = bundle[AppContract.MY_EVENT_BUNDLE] as Event
            viewModel.event = event
            updateUi()
        }
        Util.hideKeyboard(act)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker?.handleActivityResult(resultCode,requestCode, data)

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(activity?.applicationContext, data)
                    updateLocation(place)
                    Log.i(TAG, "Place: ${place.name}")
                }
                PlaceAutocomplete.RESULT_ERROR -> {
                    val status = PlaceAutocomplete.getStatus(activity?.applicationContext, data)
                    toast(R.string.could_not_get_location)
                    Log.i(TAG, status.statusMessage)
                }
                RESULT_CANCELED -> {

                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePicker?.handlePermission(requestCode, grantResults)
    }

    private fun updateUi(context: Context){
        val types = context.resources.getStringArray(R.array.party_types).toList()
        val place = viewModel.place

        if(viewModel.event.university.isNotBlank()) {
            event_university.updateTextSelector(viewModel.event.university, android.R.color.black)
        }

        if(viewModel.event.eventType.isNotBlank()) {
            event_type.updateTextSelector(viewModel.event.eventType, android.R.color.black)
        }

        if(place != null) {
            event_address.updateTextSelector(place.address, android.R.color.black)
        }

        if(viewModel.event.tickets.size == 1) {
            event_tickets.updateTextSelector(getString(R.string.ticket_quantity_one), android.R.color.black)
        } else if(viewModel.event.tickets.size > 1) {
            event_tickets.updateTextSelector(getString(R.string.ticket_quantity_with_params,
                    viewModel.event.tickets.size), android.R.color.black)
        }
        
        imagePicker = ImagePicker(activity, this@CreateEventFragment) { imageUri ->
            viewModel.imageUri = imageUri
            event_image.scaleType = ImageView.ScaleType.FIT_XY
            event_image.loadImage(imageUri)
        }

        event_save_button.setOnClickListener { save() }

        event_tickets.setOnClickListener { mCallback?.createTicket() }

        event_image.setOnClickListener { imagePicker?.choosePicture(true) }

        event_address.setOnClickListener {
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                        .build(activity)
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
            } catch (e: GooglePlayServicesRepairableException) {
                Log.d(TAG, e.message)
                Crashlytics.logException(e)
            } catch (e: GooglePlayServicesNotAvailableException) {
                Log.d(TAG, e.message)
                Crashlytics.logException(e)
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

        event_university.setOnClickListener {
            selector(getString(R.string.select_university), universities) { _, i ->
                viewModel.event.university = universities[i]
                event_university.updateTextSelector(viewModel.event.university, android.R.color.black)
            }
        }

        event_type.setOnClickListener {
            selector(getString(R.string.select_type), types) { _, i ->
                event_type.updateTextSelector(types[i], android.R.color.black)
                viewModel.event.eventType = types[i]
            }
        }
    }

    fun dateChanged(year: Int, month: Int, dayOfMonth: Int) {
        if(type == 0){
            startCalendar.set(Calendar.DAY_OF_YEAR, year)
            startCalendar.set(Calendar.MONTH, month)
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            event_start_date.text = Util.formatDate(startCalendar)
            viewModel.event.startTime = startCalendar.timeInMillis
        } else{
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
        } else{
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
        dialog.show(activity?.supportFragmentManager, null)
    }

    private fun callTimeDialog(){
        val dialog = TimePickerFragment()
        dialog.setOnTimeSetListener(object : TimePickerFragment.TimeSetListener{
            override fun timeChanged(hourOfDay: Int, minute: Int) {
                this@CreateEventFragment.timeChanged(hourOfDay, minute)
            }
        })
        dialog.show(activity?.supportFragmentManager, null)
    }

    private fun updateLocation(place: Place) {
        viewModel.place = Place()
        viewModel.place?.latLng = LatLng(place.latLng.latitude, place.latLng.longitude)
        viewModel.place?.address = place.address.toString()
        viewModel.place?.name = place.name.toString()
        viewModel.place?.id = place.id
        if(place.name.isNotEmpty() && place.address.isNotEmpty()) {
            event_address.updateTextSelector(getString(R.string.place_name_address, place.name,
                    place.address), android.R.color.black)
        }
        viewModel.event.placeId = place.id
    }

    private fun save() {
        convertViewToEvent()
        val event = viewModel.event
        val uri = viewModel.imageUri

        event.creator = creator

        if(!validate(event)) {
            Log.d(TAG, "Invalid event")
            return
        }
        val dialog = indeterminateProgressDialog(message = "", title = getString(R.string.uploading_event))
        dialog.show()
        if (uri != null) {
            Log.d(TAG, "uri is not null")
            dialog.setMessage(getString(R.string.uploading_media))
            fbasemanager.uploadEventImage(uri)?.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                val placeholder = taskSnapshot?.storage?.path?.let{
                    Media(taskSnapshot.downloadUrl.toString(), it, MEDIA_TYPE_IMAGE)
                }
                event.placeholderImage = placeholder
                completeSave(dialog, event)
            }
        }
        else{
            Log.d(TAG, "uri is null")
            completeSave(dialog, event)
        }
    }

    private fun completeSave(dialog: ProgressDialog, event: Event) {
        fbasemanager.addEvent(event)
        toast(R.string.event_saved)
        viewModel.savePlace()
        mCallback?.eventSaved(event)
        dialog.dismiss()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try{
            mCallback = context as CreateEventFragmentInterface
        } catch (e: ClassCastException){
            Log.d(TAG, e.message)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "OnSavedInstanceState")
    }

    private fun updateUi(){
        val event = viewModel.event
        startCalendar.timeInMillis = event.startTime
        endCalendar.timeInMillis = event.endTime
        event_name.setText(event.eventName)
        event_start_date.text = Util.formatDate(startCalendar)
        event_start_time.text = Util.formatTime(startCalendar)
        event_end_date.text = Util.formatDate(endCalendar)
        event_end_time.text = Util.formatTime(endCalendar)
        event_description.setText(event.description)

        if(event.eventType.isNotBlank()) {
            event_type.updateTextSelector(event.eventType, android.R.color.black)
        }

        val place = viewModel.place
        if(place != null && place.id.isNotEmpty()) {
            event_address.text = getString(R.string.place_name_address, place.name, place.address)
        }
        if(viewModel.imageUri != null){
            event_image.loadImage(viewModel.imageUri)
            event_image.scaleType = ImageView.ScaleType.FIT_XY
        } else {
            val link = event.placeholderImage?.url
            if(link != null && link.isNotEmpty()) {
                event_image.loadImage(link)
                event_image.scaleType = ImageView.ScaleType.FIT_XY
            }
        }

        updateAddress(event.placeId)

        if(event.tickets.size == 1) {
            event_tickets.updateTextSelector(getString(R.string.ticket_quantity_one), android.R.color.black)
        } else if(event.tickets.size > 1) {
            event_tickets.updateTextSelector(getString(R.string.ticket_quantity_with_params,
                    viewModel.event.tickets.size), android.R.color.black)
        }
    }

    private fun convertViewToEvent() {
        val title = event_name.text.toString().trim()
        val description = event_description.text.toString().trim()
        val eventType = event_type.text.toString()
        val startTime = startCalendar.timeInMillis
        val endTime = endCalendar.timeInMillis
        val tickets = mCallback?.getTicketList() ?: ArrayList<Ticket>()

        Log.d(TAG, "$tickets")
        val university = Util.getPrefString(act, AppContract.PREF_UNIVERSITY_KEY)

        val event = viewModel.event.apply {
            this.eventName = title
            this.eventType = eventType
            this.eventName = title
            this.eventType = eventType
            this.description = description
            this.university = university
            this.startTime = startTime
            this.endTime = endTime
            this.creator = AppContract.CREATOR
            this.tickets = tickets
            this.timeZone = Calendar.getInstance().timeZone.displayName
            this.university = university
        }

        Log.d(TAG, "${event.tickets}")
    }

    private fun validate(event: Event): Boolean {
        Log.d(TAG, "validate() called")
        var isValid = true

        if(event.eventName.isEmpty()) {
            event_name.error = getString(R.string.field_must_be_set)
            isValid = false
        }
        if(event.description.isEmpty()) {
            event_description.error = getString(R.string.field_must_be_set)
            isValid = false
        }
        if(event.placeId.isEmpty()) {
            event_address.text = getString(R.string.address_must_be_set)
            event_address.setTextColor(resources.getColor(R.color.dull_red))
            isValid = false
            return isValid
        }
        if(event.startTime >= event.endTime) {
            longSnackbar(rootView, R.string.end_date_greater_start_date).show()
            isValid = false
            return isValid
        }
        if(event.endTime <= System.currentTimeMillis()) {
            longSnackbar(rootView, R.string.event_ended).show()
            isValid = false
            return isValid
        }
        if(event.university.isEmpty()) {
            longSnackbar(rootView, R.string.university_must_be_set).show()
            isValid = false
            return isValid
        }
        return isValid
    }

    private fun updateAddress(placeId: String) {
        viewModel.getPlace(placeId)?.observe(this, Observer {
            it?.let {
                event_address.updateTextSelector(getString(R.string.place_name_address, it.name,
                        it.address), android.R.color.black)
            }
        })
    }

    private fun updateCreator(cr: Map<String, Any>) {
        creator.imageUrl = cr[AppContract.FIREBASE_USER_PHOTO_URL] as String? ?: ""
        creator.name = cr[AppContract.FIREBASE_USER_USERNAME] as String? ?: ""
        creator.stripeAccountId = cr[AppContract.STRIPE_ACCOUNT_ID] as String? ?: ""
        creator.uid = cr[AppContract.FIREBASE_USER_UID] as String? ?: ""
    }

    companion object {
        private const val STRIPE_ACCOUNT_ID = "stripeAccountId"
        private const val MEDIA_TYPE_IMAGE = "image"
    }
}