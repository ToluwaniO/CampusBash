package toluog.campusbash.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.event_card_layout.*
import kotlinx.android.synthetic.main.event_card_layout.view.*
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.utils.Util

/**
 * Created by oguns on 12/15/2017.
 */
class EventAdapter(var events: ArrayList<Event>, var context: Context?): RecyclerView.Adapter<EventAdapter.ViewHolder>(){

    val listener: OnItemClickListener

    init {
        listener = context as OnItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(event: Event)
    }

    class ViewHolder(override val containerView: View?): RecyclerView.ViewHolder(containerView),
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

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.event_card_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val event = events[position]
        holder?.bind(event, listener, context)
    }

    override fun getItemCount() = events.size

}