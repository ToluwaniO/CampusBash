package toluog.campusbash.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.event_card_layout.view.*
import toluog.campusbash.R
import toluog.campusbash.model.Event

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

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = itemView.findViewById(R.id.event_image) as ImageView
        val eventTitle: TextView = itemView.findViewById(R.id.event_title) as TextView

        fun bind(event: Event, listener: OnItemClickListener){
            itemView.setOnClickListener { listener.onItemClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.event_card_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val event = events[position]
        holder?.eventTitle?.text = event.eventName
        if(event.placeholderUrl != null){
            Glide.with(context).load(event.placeholderUrl).into(holder?.eventImage)
        }
        holder?.bind(event, listener)
    }

    override fun getItemCount() = events.size

}