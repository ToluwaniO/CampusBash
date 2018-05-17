package toluog.campusbash.view

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.myhexaville.smartimagepicker.ImagePicker
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
import com.bumptech.glide.Glide
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast
import toluog.campusbash.data.GeneralDataSource.Companion.user
import toluog.campusbash.model.*
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
    private lateinit var adapter: ArrayAdapter<CharSequence>
    private var imageUri: Uri? = null
    private lateinit var fbasemanager: FirebaseManager
    private var mCallback: CreateEventFragmentInterface? = null
    private var type = 0
    private lateinit var viewModel: CreateEventViewModel
    private lateinit var country: String
    private lateinit var university: String
    private lateinit var countries: List<String>
    private val universities = ArrayList<String>()
    var isSaved = false
    private var liveUniversities: LiveData<List<University>>? = null
    private val user = FirebaseManager.getUser()
    private var stripeAccountId: String? = null


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
        university = Util.getPrefString(activity!!, AppContract.PREF_UNIVERSITY_KEY)

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
                    val id = it[STRIPE_ACCOUNT_ID] as String?
                    stripeAccountId = id
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
                    Log.i(TAG, "Place: " + place.name)
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

    private fun updateUi(context: Context?){
        event_country.text = country
        event_university.text = university
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

        event_country_layout.setOnClickListener {
            selector(getString(R.string.select_country), countries, { _, i ->
                country = countries[i]
                event_country.text = country
                startObserver(country)
            })
        }

        event_university_layout.setOnClickListener {
            selector(getString(R.string.select_university), universities, { _, i ->
                university = universities[i]
                event_university.text = university
            })
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
            address_text.text = getString(R.string.place_name_address, place.name, place.address)
        }
        address_text.setTextColor(resources.getColor(android.R.color.black))
        viewModel.event.placeId = place.id
    }

    private fun save() {
        val title = event_name.text.toString().trim()
        val description = event_description.text.toString().trim()
        val eventType = adapter.getItem(event_type_spinner.selectedItemPosition).toString()
        val startTime = startCalendar.timeInMillis
        val endTime = endCalendar.timeInMillis
        val uri = imageUri
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

        val creator = FirebaseManager.getCreator()
        if (creator != null) event.creator = creator
        event.creator.stripeAccountId = stripeAccountId

        if(!validate(event)) {
            Log.d(TAG, "Invalid event")
            return
        }

        if (tickets.size == 0) {
            toast(R.string.must_add_1_ticket)
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
        }
        catch (e: ClassCastException){
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
        event_type_spinner.setSelection(adapter.getPosition(event.eventType))
        event_start_date.text = Util.formatDate(startCalendar)
        event_start_time.text = Util.formatTime(startCalendar)
        event_end_date.text = Util.formatDate(endCalendar)
        event_end_time.text = Util.formatTime(endCalendar)
        event_description.setText(event.description)
        val place = viewModel.place
        if(place != null && place.id.isNotEmpty()) {
            address_text.text = getString(R.string.place_name_address, place.name, place.address)
        }
        if(viewModel.imageUri != null){
            imageUri = viewModel.imageUri
            event_image.setImageURI(imageUri)
        }
        else {
            val link = event.placeholderImage?.url
            if(link != null && link.isNotEmpty()) {
                Glide.with(this).load(link).into(event_image)
            }
        }
    }

    private fun startObserver(country: String) {
        liveUniversities = viewModel.getUniversities(country)
        liveUniversities?.observe(this, Observer {
            universities.clear()
            it?.forEach { university ->
                universities.add(university.name)
            }
        })
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
            address_text.text = getString(R.string.address_must_be_set)
            address_text.setTextColor(resources.getColor(R.color.dull_red))
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

    companion object {
        private const val STRIPE_ACCOUNT_ID = "stripeAccountId"
        private const val MEDIA_TYPE_IMAGE = "image"
    }
}