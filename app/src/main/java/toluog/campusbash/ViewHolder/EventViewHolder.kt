package toluog.campusbash.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.event_card_layout.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import toluog.campusbash.R
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

class EventViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
        LayoutContainer {

    fun bind(event: Event, places: List<Place>, listener: EventAdapter.OnItemClickListener, context: Context?,
             myEvent: Boolean, noPlaceSet: ArrayMap<String, Set<Event>>){
        event_title.text = event.eventName
        //event_address.text = event.place.address
        val place = findPlace(event.placeId, places)
        if(place != null) {
            event_address.visibility = View.VISIBLE
            event_address.text = place.address
        } else {
            event_address.visibility = View.GONE
            val notHaves = noPlaceSet[event.placeId]
            if(notHaves == null) {
                noPlaceSet.put(event.placeId, setOf(event))
            } else {
                notHaves.plus(event)
            }
        }
        event_day.text = Util.getDay(event.startTime)
        event_month.text = Util.getShortMonth(event.startTime)
        if(event.placeholderImage != null){
            Glide.with(context).load(event.placeholderImage?.url).into(event_image)
        } else {
            event_image.setImageResource(R.drawable.default_event_background)
        }
        Glide.with(context).load(event.creator.imageUrl).into(event_creator_image)
        itemView.setOnClickListener { listener.onItemClick(event, it) }
        if(myEvent) {
            itemView.isLongClickable = true
            itemView.setOnLongClickListener {
                deleteEvent(context, event.eventId)
                true
            }
        }
    }

    private fun deleteEvent(context: Context?, eventId: String) {
        context?.alert ("Do you want to delete this event?") {
            yesButton {
                val fbaseManager = FirebaseManager()
                fbaseManager.deleteEvent(context, eventId)
            }
            noButton {
                it.dismiss()
            }
        }?.show()
    }

    private fun findPlace(id: String, places: List<Place>): Place? {
        val index = places.map { it.id }.indexOf(id)
        if(index >= 0) return places[index]
        return null
    }
}