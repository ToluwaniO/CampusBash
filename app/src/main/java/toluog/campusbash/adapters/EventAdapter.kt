package toluog.campusbash.adapters

import android.arch.lifecycle.LiveData
import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.gms.ads.formats.NativeAppInstallAd
import com.google.android.gms.ads.formats.NativeContentAd
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.event_card_layout.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import toluog.campusbash.R
import toluog.campusbash.ViewHolder.NativeAppInstallAdViewHolder
import toluog.campusbash.ViewHolder.NativeContentAdViewHolder
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import java.util.HashSet

/**
 * Created by oguns on 12/15/2017.
 */
class EventAdapter(var events: ArrayList<Any>, var places: List<Place>, var context: Context?,
                   var myEvents: Boolean = false): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val listener: OnItemClickListener
    private val EVENT_VIEW_TYPE = 0
    private val NATIVE_APP_INSTALL_AD_VIEW_TYPE = 1
    private val NATIVE_CONTENT_AD_VIEW_TYPE = 2
    private val TAG = EventAdapter::class.java.simpleName
    private val noPlaceSet = ArrayMap<String, Set<Event>>()

    init {
        listener = context as OnItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(event: Event, view: View)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            NATIVE_APP_INSTALL_AD_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.native_app_install_view, parent, false)
                NativeAppInstallAdViewHolder(view)
            }
            NATIVE_CONTENT_AD_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.native_ad_view, parent, false)
                NativeContentAdViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.event_card_layout, parent, false)
                EventViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        val item = events[position]
        Log.d(TAG, "VIEW TYPE is ${viewType}")

        when(viewType) {
            EVENT_VIEW_TYPE -> (holder as EventViewHolder?)?.bind(item as Event, places,
                    listener, context, myEvents, noPlaceSet)
            NATIVE_APP_INSTALL_AD_VIEW_TYPE -> (holder as NativeAppInstallAdViewHolder?)
                    ?.bind(item as NativeAppInstallAd)
            NATIVE_CONTENT_AD_VIEW_TYPE -> (holder as NativeContentAdViewHolder?)
                    ?.bind(item as NativeContentAd)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = events[position]

        if(item is NativeAppInstallAd) {
            return NATIVE_APP_INSTALL_AD_VIEW_TYPE
        } else if(item is NativeContentAd) {
            return NATIVE_CONTENT_AD_VIEW_TYPE
        }
        return EVENT_VIEW_TYPE
    }

    override fun getItemCount() = events.size

    fun notifyListChanged(newEvents: ArrayList<Any>) {
        val diffResult = DiffUtil.calculateDiff(EventsDiffCallback(events, newEvents))
        events.clear()
        events.addAll(newEvents)
        diffResult.dispatchUpdatesTo(this)
    }

    fun notifyPlaceChanged(places: List<Place>) {
        this.places = places
        for (place in places) {
            if(noPlaceSet.containsKey(place.id)) {
                noPlaceSet[place.id]?.forEach {
                    val index = events.indexOf(it)
                    notifyItemChanged(index)
                }
                noPlaceSet.remove(place.id)
            }
        }
    }

    class EventViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        fun bind(event: Event, places: List<Place>, listener: OnItemClickListener, context: Context?,
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

}