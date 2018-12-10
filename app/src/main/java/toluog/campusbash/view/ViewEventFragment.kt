package toluog.campusbash.view

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_view_event.*
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.Analytics
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.extension.*
import toluog.campusbash.view.viewmodel.ViewEventViewModel


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ViewEventFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ViewEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ViewEventFragment : BaseFragment(), OnMapReadyCallback {
    private var eventId: String = ""
    private var event: Event? = null
    private var tickets = arrayListOf<Ticket>()
    private var place: Place? = null
    private var mMap: GoogleMap? = null
    private val TAG = ViewEventActivity::class.java.simpleName
    private var liveEvent: LiveData<Event>? = null
    private lateinit var viewModel: ViewEventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        parseArgs()
        viewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)
        liveEvent = viewModel.getEvent(eventId)
        observeEvent()
        observeTickets()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.view_event_menu, menu)
        updateMenu(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun parseArgs() {
        val fragmentArgs = ViewEventFragmentArgs.fromBundle(arguments ?: act.intent.extras)
        eventId = fragmentArgs.eventId
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        val pl = place

        if(pl != null) {
            updateLocation(pl)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id) {
            R.id.action_share -> {
                startActivity(intentFor<ShareEventActivity>().apply {
                    putExtras(bundleOf(AppContract.MY_EVENT_BUNDLE to event))
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
            event_image.lazyLoadImage(actCompat, R.drawable.default_event_background)
        } else{
            event_image.lazyLoadImage(actCompat, event.placeholderImage?.url)
        }
        event_title.text = event.eventName
        event_description.text = event.description
        host_image.lazyLoadImage(actCompat, event.creator.imageUrl)
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
            act.invalidateOptionsMenu()
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
}
