package toluog.campusbash.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_view_event.*
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import java.util.*

class ViewEventActivity : AppCompatActivity() {

    private lateinit var event: Event
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)

        val bundle = intent.extras
        event = bundle.getParcelable(AppContract.MY_EVENT_BUNDLE) as Event

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
