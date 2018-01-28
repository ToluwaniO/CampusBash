package toluog.campusbash.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.google.android.gms.ads.formats.NativeAppInstallAd
import com.google.android.gms.ads.formats.NativeContentAd
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.event_card_layout.*
import kotlinx.android.synthetic.main.event_card_layout.view.*
import toluog.campusbash.R
import toluog.campusbash.ViewHolder.NativeAppInstallAdViewHolder
import toluog.campusbash.ViewHolder.NativeContentAdViewHolder
import toluog.campusbash.model.Event
import toluog.campusbash.utils.Util

/**
 * Created by oguns on 12/15/2017.
 */
class EventAdapter(var events: ArrayList<Any>, var context: Context?): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val listener: OnItemClickListener
    private val EVENT_VIEW_TYPE = 0
    private val NATIVE_APP_INSTALL_AD_VIEW_TYPE = 1
    private val NATIVE_CONTENT_AD_VIEW_TYPE = 2

    init {
        listener = context as OnItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(event: Event)
    }

    class EventViewHolder(override val containerView: View?): RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        fun bind(event: Event, listener: OnItemClickListener, context: Context?){
            event_title.text = event.eventName
            event_address.text = event.locationAddress
            event_day.text = Util.getDay(event.startTime)
            event_month.text = Util.getShortMonth(event.startTime)
            if(event.placeholderUrl != null){
                Glide.with(context).load(event.placeholderUrl).into(event_image)
            } else {
                event_image.setImageResource(R.drawable.default_event_background)
            }
            Glide.with(context).load(event.creator.imageUrl).into(event_creator_image)
            itemView.setOnClickListener { listener.onItemClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType) {
            NATIVE_APP_INSTALL_AD_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.native_app_install_view, parent, false)
                NativeAppInstallAdViewHolder(view)
            }
            NATIVE_CONTENT_AD_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.native_ad_view, parent, false)
                NativeContentAdViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.event_card_layout, parent, false)
                EventViewHolder(view)
            }
        }


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewType = getItemViewType(position)
        val item = events[position]

        when(viewType) {
            EVENT_VIEW_TYPE -> (holder as EventViewHolder?)?.bind(item as Event, listener, context)
            NATIVE_APP_INSTALL_AD_VIEW_TYPE -> (holder as NativeAppInstallAdViewHolder?)
                    ?.bind(item as NativeAppInstallAd)
            NATIVE_CONTENT_AD_VIEW_TYPE -> (holder as NativeContentAdViewHolder?)
                    ?.bind(item as NativeContentAd)
        }
    }
    override fun getItemViewType(position: Int): Int {
        val items = events[position]

        return when(items) {
            items is NativeAppInstallAd -> NATIVE_APP_INSTALL_AD_VIEW_TYPE
            items is NativeContentAd -> NATIVE_CONTENT_AD_VIEW_TYPE
            else -> EVENT_VIEW_TYPE
        }
    }

    override fun getItemCount() = events.size

}