package toluog.campusbash.view

import android.arch.lifecycle.ViewModelProviders
import android.arch.lifecycle.Observer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_view_event.*
import org.jetbrains.anko.act
import org.jetbrains.anko.intentFor
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import java.util.*

class ViewEventActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var eventId: String
    private var event: Event? = null
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bundle = intent.extras
        eventId = bundle.getString(AppContract.MY_EVENT_BUNDLE)
        val viewModel: ViewEventViewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)
        viewModel.getEvent(eventId)?.observe(this, Observer { event ->
            this.event = event
            onMapReady(mMap)
            if(event != null) updateUi(event)
        })

        event_get_ticket.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(AppContract.MY_EVENT_BUNDLE, event?.eventId)
            startActivity(intentFor<BuyTicketActivity>().putExtras(bundle)) }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        val ev = event

        if(ev != null) {
            val latLng = LatLng(ev.place.latLng.lat, ev.place.latLng.lon)
            mMap?.addMarker(MarkerOptions().position(latLng))
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(16f))
        }
    }

    private fun updateUi(event: Event){
        val calA = Calendar.getInstance()
        val calB = Calendar.getInstance()
        calA.timeInMillis = event.startTime
        calB.timeInMillis = event.endTime

        if(event.placeholderImage == null || TextUtils.isEmpty(event.placeholderImage?.url)){

        } else{
            Glide.with(this).load(event.placeholderImage?.url).into(event_image)
        }
        event_title.text = event.eventName
        event_description.text = event.description
        Glide.with(this).load(event.creator.imageUrl).into(host_image)
        event_creator.text = "hosted by ${event.creator.name}"
        event_time.text = "${Util.formatDateTime(calA)} - ${Util.formatDateTime(calB)}"
        place_name.text = event.place.name
        address_text.text = event.place.address

        when {
            event.tickets.size == 0 -> ticket_text.text = getString(R.string.free_event)
            event.tickets.size == 1 -> {
                val ticket = event.tickets[0]
                ticket_text.text = "One ticket type available for $${ticket.price}"
            }
            else -> ticket_text.text = "${event.tickets.size} ticket types available"
        }
    }
}
