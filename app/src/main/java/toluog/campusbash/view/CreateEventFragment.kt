package toluog.campusbash.view

import android.os.Bundle
import android.view.View
import com.myhexaville.smartimagepicker.ImagePicker
import android.app.Activity.RESULT_OK
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.create_event_layout.*
import toluog.campusbash.R
import java.lang.ClassCastException
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import toluog.campusbash.utils.AppContract.Companion.PLACE_AUTOCOMPLETE_REQUEST_CODE
import com.google.android.gms.location.places.Place
import android.app.Activity.RESULT_CANCELED
import android.app.ProgressDialog
import androidx.lifecycle.Observer
import android.net.Uri
import android.widget.ImageView
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.support.v4.*
import toluog.campusbash.model.*
import toluog.campusbash.utils.*
import toluog.campusbash.view.viewmodel.CreateEventViewModel
import java.util.Calendar
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * Created by oguns on 12/13/2017.
 */
class CreateEventFragment : BaseFragment() {

    private val TAG = CreateEventFragment::class.java.simpleName
    private lateinit var rootView: View
    private val startCalendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
    }
    private val endCalendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
    }
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
    private val eventTypes = arrayListOf<String>()

    private val threadJob = Dispatchers.Default
    private val threadScope = CoroutineScope(threadJob)


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
        eventTypes.addAll(resources.getStringArray(R.array.party_types))

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
                    val flags = it[AppContract.FIREBASE_USER_ACCOUNT_FLAGS] as List<String>? ?: emptyList()
                    eventTypes.clear()
                    eventTypes.addAll(resources.getStringArray(R.array.party_types))
                    if(flags.contains("facultyAccount")) {
                        eventTypes.addAll(resources.getStringArray(R.array.faculty_party_types))
                    }
                }
            })
        }
        updateUi(view.context)
        if(isSaved) updateUi()
        else if(bundle != null) {
            val event = bundle[AppContract.MY_EVENT_BUNDLE] as Event
            val tickets = bundle[AppContract.TICKETS] as ArrayList<Ticket>
            viewModel.event = event
            viewModel.tickets = tickets
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
        } else if (requestCode == UNIVERSITY_REQUEST_CODE) {
            viewModel.event.universities.clear()
            when (resultCode) {
                RESULT_OK -> {
                    val unis = data?.extras?.get(AppContract.UNIVERSITIES) as Array<String>? ?: emptyArray()
                    viewModel.event.universities.addAll(unis)
                    updateUniversities(viewModel.event.universities)
                    Log.d(TAG, "${viewModel.event.universities}")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePicker?.handlePermission(requestCode, grantResults)
    }

    private fun updateUi(context: Context){
        val place = viewModel.place

        updateUniversities(viewModel.event.universities)

        if(viewModel.event.eventType.isNotBlank()) {
            event_type.updateTextSelector(viewModel.event.eventType, android.R.color.black)
        }

        if(place != null) {
            event_address.updateTextSelector(place.address, android.R.color.black)
        }

        if(viewModel.tickets.size == 1) {
            event_tickets.updateTextSelector(getString(R.string.ticket_quantity_one), android.R.color.black)
        } else if(viewModel.tickets.size > 1) {
            event_tickets.updateTextSelector(getString(R.string.ticket_quantity_with_params,
                    viewModel.tickets.size), android.R.color.black)
        }
        
        imagePicker = ImagePicker(activity, this@CreateEventFragment) { imageUri ->
            updateImage(imageUri)
        }.setWithImageCrop(16, 9)

        event_save_button.setOnClickListener { save() }

        event_tickets.setOnClickListener { mCallback?.createTicket() }

        event_image.setOnClickListener { eventImageClicked() }

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
            startActivityForResult(intentFor<SelectUniversityActivity>().apply {
                putExtra(AppContract.UNIVERSITIES, viewModel.event.universities)
            }, UNIVERSITY_REQUEST_CODE)
        }

        event_type.setOnClickListener {
            selector(getString(R.string.select_type), eventTypes) { _, i ->
                event_type.updateTextSelector(eventTypes[i], android.R.color.black)
                viewModel.event.eventType = eventTypes[i]
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
        if(place.name.isNotEmpty() && place.address?.isNotEmpty() == true) {
            event_address.updateTextSelector(getString(R.string.place_name_address, place.name,
                    place.address), android.R.color.black)
        }
        viewModel.event.placeId = place.id
    }

    private fun updateUniversities(unis: List<String>) {
        when {
            unis.isEmpty() -> {
                event_university.updateTextSelector(getString(R.string.select_university), R.color.really_light_gray)
            }
            unis.size == 1 -> {
                event_university.updateTextSelector(getString(R.string.one_university_selected), android.R.color.black)
            }
            else -> {
                event_university.updateTextSelector(getString(R.string.x_universities_selected,
                        unis.size), android.R.color.black)
            }
        }
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
            val pair = fbasemanager.uploadEventImage(uri)
            pair.second?.continueWithTask { task ->
                if(!task.isSuccessful) {
                    Log.d(TAG, task.exception?.message)
                    throw task.exception ?: Exception("Upload task was not successful")
                }
                pair.first?.downloadUrl
            }?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val placeholder = Media(task.result.toString(), pair.first?.path ?: "", MEDIA_TYPE_IMAGE)
                    event.placeholderImage = placeholder
                    this.launch { completeSave(dialog, event) }
                } else {
                    Log.d(TAG, task.exception?.message)
                }
            }

        }
        else{
            Log.d(TAG, "uri is null")
            this.launch { completeSave(dialog, event) }
        }
    }

    private suspend fun completeSave(dialog: ProgressDialog, event: Event) {
        val eventResult = fbasemanager.addEvent(event)
        when (eventResult) {
            is FirebaseManager.FirebaseOperationResult.Success -> {
                saveTickets()
                withContext(Dispatchers.Main) {
                    toast(R.string.event_saved)
                    viewModel.savePlace()
                    mCallback?.eventSaved(event)
                    dialog.dismiss()
                }
            }
            is FirebaseManager.FirebaseOperationResult.Error -> {
                withContext(Dispatchers.Main) {
                    toast(R.string.event_save_failed)
                    Log.d(TAG, "$event")
                    Log.d(TAG, "Failed to save event")
                    Log.d(TAG, eventResult.exception.message)
                }
            }
        }
    }

    private suspend fun saveTickets() {
        if (viewModel.tickets.isEmpty()) {
            viewModel.tickets.add(
                    Ticket().apply {
                        this.name = "FREE"
                        this.quantity = Long.MAX_VALUE
                        this.description = "Free ticket"
                        this.type = "free"
                    }
            )
        }
        fbasemanager.addTickets(viewModel.tickets, viewModel.event.eventId)
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
        updateUniversities(viewModel.event.universities)

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

        if(viewModel.tickets.size == 1) {
            event_tickets.updateTextSelector(getString(R.string.ticket_quantity_one), android.R.color.black)
        } else if(viewModel.tickets.size > 1) {
            event_tickets.updateTextSelector(getString(R.string.ticket_quantity_with_params,
                    viewModel.tickets.size), android.R.color.black)
        }

    }

    private fun convertViewToEvent() {
        val title = event_name.text.toString().trim()
        val description = event_description.text.toString().trim()
        val eventType = event_type.text.toString()
        val startTime = startCalendar.timeInMillis
        val endTime = endCalendar.timeInMillis
        val tickets = mCallback?.getTicketList() ?: ArrayList()

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
            this.timeZone = Calendar.getInstance().timeZone.displayName
            this.university = university
        }

        Log.d(TAG, "${viewModel.tickets}")
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

    private fun eventImageClicked() {
        val options = listOf("Add new image", "Remove image")
        val link = viewModel.event.placeholderImage?.url
        if ((link != null && link.isNotBlank()) || viewModel.imageUri != null) {
            selector(null, options) { _, i ->
                if (i == 0) {
                    imagePicker?.choosePicture(true)
                } else {
                    event_image.setImageResource(R.drawable.ic_add_image)
                    event_image.scaleType = ImageView.ScaleType.CENTER
                    viewModel.event.placeholderImage = null
                    viewModel.imageUri = null
                }
            }
        } else {
            imagePicker?.choosePicture(true)
        }
    }

    private fun updateImage(imageUri: Uri) {
        threadScope.launch {
            if (Util.validImageSize(imageUri, activity?.applicationContext)) {
                withContext(Dispatchers.Main) {
                    viewModel.imageUri = imageUri
                    event_image.scaleType = ImageView.ScaleType.FIT_XY
                    event_image.loadImage(imageUri)
                }
            } else {
                withContext(Dispatchers.Main) {
                    snackbar(root_view, R.string.max_image_size)
                }
            }
        }
    }

    companion object {
        private const val MEDIA_TYPE_IMAGE = "image"
        const val UNIVERSITY_REQUEST_CODE = 3438
    }
}