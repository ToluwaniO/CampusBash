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
import com.crashlytics.android.Crashlytics
import org.jetbrains.anko.toast
import toluog.campusbash.BuildConfig
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.coroutines.*
import toluog.campusbash.model.Place
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.*
import toluog.campusbash.view.viewmodel.ViewEventViewModel

class ViewEventActivity : AppCompatActivity(), OnMapReadyCallback {

    private var eventId: String = ""
    private var event: Event? = null
    private var tickets = arrayListOf<Ticket>()
    private var place: Place? = null
    private var mMap: GoogleMap? = null
    private val TAG = ViewEventActivity::class.java.simpleName
    private var liveEvent: LiveData<Event>? = null
    private lateinit var viewModel: ViewEventViewModel

    private val threadJob = Dispatchers.Default
    private val threadScope = CoroutineScope(threadJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportPostponeEnterTransition()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)
        threadScope.launch {
            checkForDynamicLink()
            withContext(Dispatchers.Main) {
                observeEvent()
                observeTickets()
            }
        }
    }

    private suspend fun checkForDynamicLink() {
        val bundle = intent.extras

        try {
            val pendingDynamicLinkData = FirebaseManager.getDynamicLinkData(intent)
            val deepLink: Uri?
            if (bundle?.getString(AppContract.MY_EVENT_BUNDLE) == null) {
                deepLink = pendingDynamicLinkData.link
                eventId = deepLink?.getQueryParameter("eventId") ?: ""
                viewModel.downloadEvent(eventId)
                liveEvent = viewModel.getEvent(eventId)
            } else {
                Log.d(TAG, "Deep link data is null")
                eventId = bundle.getString(AppContract.MY_EVENT_BUNDLE) ?: ""
                liveEvent = viewModel.getEvent(eventId)
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message)
            Crashlytics.logException(e)
            toast(R.string.error_occurred)
            finish()
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
            R.id.action_share -> {
                startActivity(intentFor<ShareEventActivity>().apply {
                    putExtras(Bundle().apply {
                        putParcelable(AppContract.MY_EVENT_BUNDLE, event)
                    })
                })
            }
            R.id.action_edit -> {
                val bundle = Bundle()
                bundle.putParcelable(AppContract.MY_EVENT_BUNDLE, event)
                bundle.putParcelableArrayList(AppContract.TICKETS, tickets)
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

        see_more_button.setOnClickListener {
            startActivity(intentFor<SeeMoreActivity>().putExtras(
                    Bundle().apply {
                        putString(AppContract.MORE_TEXT, event.description)
                    }
            ))
        }

        event_get_ticket.setOnClickListener {
            if (FirebaseManager.isSignedIn()) {
                Analytics.logBuyTicketClicked(event)
                val bundle = Bundle()
                bundle.putString(AppContract.MY_EVENT_BUNDLE, event.eventId)
                startActivity(intentFor<BuyTicketActivity>().putExtras(bundle))
            } else {
                startActivity(intentFor<MainActivity>())
            }
        }
    }

    private fun updateTickets(tickets: List<Ticket>) {
        if(tickets.isNotEmpty()) {
            get_ticket_layout.visibility = View.VISIBLE
        } else {
            get_ticket_layout.visibility = View.GONE
        }

        when {
            tickets.isEmpty() -> {
                ticket_text.text = this.getString(R.string.free_event)
            }
            tickets.size == 1 && tickets[0].isVisible -> {
                val ticket = tickets[0]
                if(ticket.type == AppContract.TYPE_FREE) {
                    ticket_text.text = this.getString(R.string.free_event)
                } else {
                    ticket_text.text = getString(R.string.price, tickets[0].currency, tickets[0].price)
                }
            }
            else -> {
                var min = tickets[0].price
                var max = tickets[0].price
                var currency = tickets[0].currency
                tickets.forEach {
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

    private fun observeTickets() {
        viewModel.getTickets(eventId).observe(this, Observer {
            this.tickets.clear()
            this.tickets.addAll(it ?: emptyList())
            updateTickets(it ?: emptyList())
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

    override fun onDestroy() {
        threadJob.cancel()
        super.onDestroy()
    }

    companion object {
        private const val DEBUG_DYNAMIC_LINK = "m88p6.app.goo.gl"
        private const val PROD_DYNAMIC_URL = "hx87a.app.goo.gl"
        private const val CAMPUSBASH_LINK = "campusbash-e0ca8.firebaseapp.com"
    }
}
