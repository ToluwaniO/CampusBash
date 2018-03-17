package toluog.campusbash.view

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.arch.lifecycle.Observer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_view_event.*
import org.jetbrains.anko.intentFor
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import android.content.Intent
import android.util.Log
import org.jetbrains.anko.toast
import toluog.campusbash.BuildConfig
import com.google.android.gms.maps.model.CameraPosition
import toluog.campusbash.utils.FirebaseManager

class ViewEventActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var eventId: String
    private var event: Event? = null
    private var mMap: GoogleMap? = null
    private val TAG = ViewEventActivity::class.java.simpleName
    private var liveEvent: LiveData<Event>? = null
    private lateinit var viewModel: ViewEventViewModel
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)
        val bundle = intent.extras

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    var deepLink: Uri? = null
                    if (pendingDynamicLinkData != null && bundle.getString(AppContract.MY_EVENT_BUNDLE) == null) {
                        deepLink = pendingDynamicLinkData.link
                        eventId = deepLink.getQueryParameter("eventId")
                        liveEvent = viewModel.getEvent(eventId)
                        observeEvent()
                    } else {
                        Log.d(TAG, "Deep link data is null")
                        eventId = bundle.getString(AppContract.MY_EVENT_BUNDLE)
                        liveEvent = viewModel.getEvent(eventId)
                        observeEvent()
                    }
                }
                .addOnFailureListener(this) { e ->
                    Log.d(TAG, "getDynamicLink:onFailure", e)
                }

        event_get_ticket.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(AppContract.MY_EVENT_BUNDLE, event?.eventId)
            startActivity(intentFor<BuyTicketActivity>().putExtras(bundle)) }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        val ev = event

        if(ev != null) {
            updateLocation(ev)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_event_menu, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id) {
            android.R.id.home -> finish()
            R.id.action_share -> share()
            R.id.action_edit -> {
                val bundle = Bundle()
                bundle.putParcelable(AppContract.MY_EVENT_BUNDLE, event)
                startActivity(intentFor<CreateEventActivity>().putExtras(bundle))
            }
        }
        return true
    }

    private fun updateUi(event: Event){
        if(event.placeholderImage == null || TextUtils.isEmpty(event.placeholderImage?.url)){

        } else{
            Glide.with(this).load(event.placeholderImage?.url).into(event_image)
        }
        event_title.text = event.eventName
        event_description.text = event.description
        Glide.with(this).load(event.creator.imageUrl).into(host_image)
        event_creator.text = "hosted by ${event.creator.name}"
        event_time.text = "${Util.getPeriod(event.startTime, event.endTime)}"
        place_name.text = event.place.name
        address_text.text = event.place.address

        when {
            event.tickets.size == 1 -> {
                val ticket = event.tickets[0]
                if(ticket.type == "free") {
                    ticket_text.text = this.getString(R.string.free_event)
                } else {
                    ticket_text.text = "One ticket type available for $${ticket.price}"
                }
            }
            else -> {
                var min = event.tickets[0].price
                var max = event.tickets[0].price
                var currency = ""
                event.tickets.forEach {
                    if(it.price < min && it.isVisible) {
                        min = it.price
                    }
                    if (it.price > max && it.isVisible) {
                        max = it.price
                        currency = it.currency
                    }
                }
                if(event.tickets.any { it.price == 0.0 && it.isVisible }) {
                    ticket_text.text = "FREE - $currency$max"
                } else {
                    ticket_text.text = "$currency$min - $currency$max"
                }

            }
        }
    }

    private fun share() {
        val event = this.event
        if(event != null) {
            val builder = Uri.Builder()
                    .scheme("https")
                    .authority("campusbash-e0ca8.firebaseapp.com")
                    .path("/")
                    .appendQueryParameter("eventId", eventId)
            val url = builder.build()
            Log.d(TAG, url.toString())
            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(url)
                    .setDynamicLinkDomain("hx87a.app.goo.gl")
                    // Open links with this app on Android
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                    .setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder()
                            .setTitle(event.eventName)
                            .setDescription(event.description)
                            .setImageUrl(Uri.parse(event.placeholderImage?.url))
                            .build())
                    .buildDynamicLink()
            val dynamicLinkUri = dynamicLink.uri
            var finalUrl = dynamicLinkUri.toString()
            finalUrl = Util.fixLink(finalUrl)
            if(BuildConfig.DEBUG) {
                finalUrl += "&d=1"
            }
            Log.d(TAG, finalUrl)

            FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(finalUrl))
                .buildShortDynamicLink()
                .addOnCompleteListener {task ->
                    if(task.isSuccessful) {
                        val shortLink = task.result.shortLink
                        val shortUrl = shortLink.toString()
                        //val flowchartLink = task.result.previewLink
                        Log.d(TAG, shortUrl)
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shortUrl)
                        startActivity(Intent.createChooser(shareIntent, "Share link using"))
                    } else {
                        Log.d(TAG, "An error occurred getting the shortLink\n${task.exception?.message}")
                        toast("An error occurred")
                    }
                }

        }

    }

    private fun observeEvent() {
        liveEvent?.observe(this, Observer { event ->
            this.event = event
            setMenuButtons()
            if(event != null) {
                updateUi(event)
                updateLocation(event)
            }
        })
    }

    private fun updateLocation(event: Event) {
        val latLng = LatLng(event.place.latLng.lat, event.place.latLng.lon)
        mMap?.addMarker(MarkerOptions().position(latLng))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(16f)
                .build()
        mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun setMenuButtons() {
        val menu = menu
        val event = event
        if (menu != null && event != null) {
            val edit = menu.findItem(R.id.action_edit)
            if(event.creator.uid != FirebaseManager.getCreator()?.uid) {
                edit.isVisible = false
            }
        }
    }
}
