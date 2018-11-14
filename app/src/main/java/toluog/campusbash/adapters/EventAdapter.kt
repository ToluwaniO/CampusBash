package toluog.campusbash.adapters

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.formats.NativeAppInstallAd
import com.google.android.gms.ads.formats.NativeContentAd
import toluog.campusbash.R
import toluog.campusbash.ViewHolder.EventViewHolder
import toluog.campusbash.ViewHolder.FeaturedViewHolder
import toluog.campusbash.ViewHolder.NativeAppInstallAdViewHolder
import toluog.campusbash.ViewHolder.NativeContentAdViewHolder
import toluog.campusbash.model.Event
import toluog.campusbash.model.Featured
import toluog.campusbash.model.Place

/**
 * Created by oguns on 12/15/2017.
 */
class EventAdapter(var events: ArrayList<Any>, var places: List<Place>, var context: Context?,
                   var myEvents: Boolean = false, var featured: Boolean = false): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val listener: OnItemClickListener
    private val EVENT_VIEW_TYPE = 0
    private val NATIVE_APP_INSTALL_AD_VIEW_TYPE = 1
    private val NATIVE_CONTENT_AD_VIEW_TYPE = 2
    private val FEATURED_VIEW_TYPE = 3
    private val TAG = EventAdapter::class.java.simpleName
    private val noPlaceSet = ArrayMap<String, Set<Event>>()
    private var featuredAdapter: EventAdapter? = null
    private var lastFeaturedList = arrayListOf<Any>()

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
            FEATURED_VIEW_TYPE -> {
                if(featuredAdapter == null) {
                    featuredAdapter = EventAdapter(arrayListOf(), places, context, false,
                            true)
                }
                val view = LayoutInflater.from(parent.context).inflate(R.layout.featured_events_layout,
                        parent, false)
                FeaturedViewHolder(view)
            }
            else -> {
                val viewId = if(featured) {
                    R.layout.event_card_layout_small
                } else {
                    R.layout.event_card_layout
                }
                val view = LayoutInflater.from(parent.context).inflate(viewId, parent, false)
                EventViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        val item = events[position]
        Log.d(TAG, "VIEW TYPE is $viewType")

        when(viewType) {
            EVENT_VIEW_TYPE -> (holder as EventViewHolder?)?.bind(item as Event, places,
                    listener, context, myEvents, noPlaceSet)
            FEATURED_VIEW_TYPE -> (holder as FeaturedViewHolder?)?.bind(item as Featured, places,
                    context, featuredAdapter)
            NATIVE_APP_INSTALL_AD_VIEW_TYPE -> (holder as NativeAppInstallAdViewHolder?)
                    ?.bind(item as NativeAppInstallAd)
            NATIVE_CONTENT_AD_VIEW_TYPE -> (holder as NativeContentAdViewHolder?)
                    ?.bind(item as NativeContentAd)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = events[position]

        return when (item) {
            is NativeAppInstallAd -> NATIVE_APP_INSTALL_AD_VIEW_TYPE
            is NativeContentAd -> NATIVE_CONTENT_AD_VIEW_TYPE
            is Featured -> FEATURED_VIEW_TYPE
            else -> EVENT_VIEW_TYPE
        }
    }

    override fun getItemCount() = events.size

    fun notifyListChanged(newEvents: ArrayList<Any>) {
        val diffResult = DiffUtil.calculateDiff(EventsDiffCallback(events, newEvents, lastFeaturedList))
        events.clear()
        events.addAll(newEvents)
        diffResult.dispatchUpdatesTo(this)
        if(newEvents.isNotEmpty() && newEvents[0] is Featured) {
            lastFeaturedList.clear()
            lastFeaturedList.addAll((newEvents[0] as Featured).events)
        }
    }

    fun notifyPlaceChanged(places: List<Place>) {
        this.places = places
        for (place in places) {
            if(noPlaceSet.containsKey(place.id)) {
                noPlaceSet[place.id]?.forEach {
                    val index = if(events.isNotEmpty() && events.first() is Featured) {
                        events.subList(1, events.size).indexOf(it)
                    } else {
                        events.indexOf(it)
                    }
                    notifyItemChanged(index)
                }
                noPlaceSet.remove(place.id)
            }
        }
    }

}