package toluog.campusbash.view

import android.arch.lifecycle.ViewModelProviders
import android.arch.lifecycle.Observer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_view_event.*
import org.jetbrains.anko.intentFor
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import java.util.*

class ViewEventActivity : AppCompatActivity() {

    private lateinit var eventId: String
    private var event: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)

        val bundle = intent.extras
        eventId = bundle.getString(AppContract.MY_EVENT_BUNDLE)
        val viewModel: ViewEventViewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)
        viewModel.getEvent(eventId)?.observe(this, Observer { event ->
            this.event = event
            if(event != null) updateUi(event)
        })


        event_get_ticket.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(AppContract.MY_EVENT_BUNDLE, event?.eventId)
            startActivity(intentFor<BuyTicketActivity>().putExtras(bundle)) }
    }

    private fun updateUi(event: Event){
        if(event.placeholderUrl == null || TextUtils.isEmpty(event.placeholderUrl)){

        } else{
            Glide.with(this).load(event.placeholderUrl).into(event_image)
        }
        event_title.text = event.eventName
        event_description.text = event.description
        event_creator.text = "by ${event.creator.name}"
        event_time.text = "${Util.formatDateTime(Date(event.startTime))} - ${Util.formatDateTime(Date(event.endTime))}"
    }
}
