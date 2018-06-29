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
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import android.content.Intent
import android.util.Log
import android.view.View
import org.jetbrains.anko.toast
import toluog.campusbash.BuildConfig
import com.google.android.gms.maps.model.CameraPosition
import toluog.campusbash.model.Place
import toluog.campusbash.utils.*

class ViewEventActivity : AppCompatActivity(), OnMapReadyCallback {

    private var eventId: String = ""
    private var event: Event? = null
    private var place: Place? = null
    private var mMap: GoogleMap? = null
    private val TAG = ViewEventActivity::class.java.simpleName
    private var liveEvent: LiveData<Event>? = null
    private lateinit var viewModel: ViewEventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportPostponeEnterTransition()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)
        val bundle = intent.extras

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    val deepLink: Uri?
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
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        val pl = place

        if(pl != null) {
            updateLocation(pl)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_event_menu, menu)
        updateMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id) {
            android.R.id.home -> onBackPressed()
            R.id.action_share -> share()
            R.id.action_edit -> {
                val bundle = Bundle()
                bundle.putParcelable(AppContract.MY_EVENT_BUNDLE, event)
                startActivity(intentFor<CreateEventActivity>().putExtras(bundle))
            }
            R.id.action_dashboard -> {
                startActivity(intentFor<EventDashboardActivity>().apply {
                    putExtra(AppContract.EVENT_ID, eventId)
                })
            }
        }
        return true
    }

    private fun updateUi(event: Event){
        if(event.placeholderImage == null || TextUtils.isEmpty(event.placeholderImage?.url)){
            event_image.lazyLoadImage(this@ViewEventActivity, R.drawable.default_event_background)
        } else{
            event_image.lazyLoadImage(this@ViewEventActivity, event.placeholderImage?.url)
        }
        event_title.text = event.eventName
        event_description.text = event.description
        host_image.lazyLoadImage(this@ViewEventActivity, event.creator.imageUrl)
        event_creator.text = getString(R.string.hosted_by_x, event.creator.name)
        event_time.text = Util.getPeriod(event.startTime, event.endTime)

        when {
            event.tickets.size == 1 && event.tickets[0].isVisible -> {
                val ticket = event.tickets[0]
                if(ticket.type == AppContract.TYPE_FREE) {
                    ticket_text.text = this.getString(R.string.free_event)
                } else {
                    ticket_text.text = getString(R.string.price, event.tickets[0].currency, event.tickets[0].price)
                }
            }
            else -> {
                var min = event.tickets[0].price
                var max = event.tickets[0].price
                var currency = event.tickets[0].currency
                event.tickets.forEach {
                    if(it.price < min && it.isVisible) {
                        min = it.price
                    }
                    if (it.price > max && it.isVisible) {
                        max = it.price
                        currency = it.currency
                    }
                }
                Log.d(TAG, "$currency $max")

                if(min < max) {
                    ticket_text.text = getString(R.string.minPrice_to_maxPrice, currency, min, currency, max)
                } else {
                    ticket_text.text = getString(R.string.price, currency, max)
                }
            }
        }

        see_more_button.setOnClickListener {
            startActivity(intentFor<SeeMoreActivity>().putExtras(
                    Bundle().apply {
                        putString(AppContract.MORE_TEXT, event.description)
                    }
            ))
        }

        event_get_ticket.setOnClickListener {
            Analytics.logBuyTicketClicked(event)
            val bundle = Bundle()
            bundle.putString(AppContract.MY_EVENT_BUNDLE, event.eventId)
            startActivity(intentFor<BuyTicketActivity>().putExtras(bundle))
        }
    }

    private fun share() {
        val event = this.event
        if(event != null) {
            val domain = if(BuildConfig.FLAVOR.equals("dev")) {
                DEBUG_DYNAMIC_LINK
            } else {
                PROD_DYNAMIC_URL
            }
            val builder = Uri.Builder()
                    .scheme("https")
                    .authority(CAMPUSBASH_LINK)
                    .path("/")
                    .appendQueryParameter("eventId", eventId)
            val url = builder.build()
            Log.d(TAG, url.toString())
            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(url)
                    .setDynamicLinkDomain(domain)
                    // Open links with this app on Android
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                    .setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder()
                            .setTitle(event.eventName)
                            .setDescription(event.description)
                            .setImageUrl(Uri.parse(event.placeholderImage?.url ?: ""))
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
                        toast(R.string.error_occurred)
                    }
                }

        }

    }

    private fun observeEvent() {
        liveEvent?.observe(this, Observer { event ->
            this.event = event
            if(event != null) {
                updateUi(event)
                observePlace(event.placeId)
            }
            invalidateOptionsMenu()
        })
    }

    private fun updateLocation(place: Place) {
        location_layout.visibility = View.VISIBLE
        place_name.text = place.name
        address_text.text = place.address
        val latLng = LatLng(place.latLng.lat, place.latLng.lon)
        mMap?.addMarker(MarkerOptions().position(latLng))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(16f)
                .build()
        mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun updateMenu(menu: Menu?) {
        if(event?.creator?.uid == FirebaseManager.getUser()?.uid) {
            menu?.findItem(R.id.action_dashboard)?.isVisible = true
            menu?.findItem(R.id.action_edit)?.isVisible = true
        }
    }

    private fun observePlace(id: String) {
        viewModel.getPlace(id)?.observe(this, Observer {
            place = it
            if(it != null) {
                updateLocation(it)
            }
        })
    }
    
    companion object {
        private const val DEBUG_DYNAMIC_LINK = "m88p6.app.goo.gl"
        private const val PROD_DYNAMIC_URL = "hx87a.app.goo.gl"
        private const val CAMPUSBASH_LINK = "campusbash-e0ca8.firebaseapp.com"
    }
}
