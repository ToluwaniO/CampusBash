package toluog.campusbash.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.bought_ticket_layout.*
import toluog.campusbash.R
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.utils.loadImage

class BoughtTicketAdapter(var tickets: List<BoughtTicket>): RecyclerView.Adapter<BoughtTicketAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.bought_ticket_layout, parent,
                false)
        return ViewHolder(v)
    }

    override fun getItemCount() = tickets.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tickets[position])
    }


    class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(ticket: BoughtTicket) {
            if(ticket.ticketCodes.isNotEmpty()) {
                event_ticket.loadImage(ticket.placeholderImage.url)
            }
            event_name.text = ticket.eventName
            tickets_number.text = if(ticket.ticketCodes.size > 1) {
                "${ticket.ticketCodes.size} tickets"
            } else {
                "${ticket.ticketCodes.size} ticket"
            }
        }
    }

}