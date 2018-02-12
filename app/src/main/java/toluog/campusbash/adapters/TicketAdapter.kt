package toluog.campusbash.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.ticket_item_layout.*
import toluog.campusbash.R
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/25/2017.
 */
class TicketAdapter(private val tickets: ArrayList<Ticket>, val context: Context): RecyclerView.Adapter<TicketAdapter.ViewHolder>() {

    private val listener: OnTicketClickListener

    init {
        listener = context as OnTicketClickListener
    }

    interface OnTicketClickListener {
        fun onTicketClick(ticket: Ticket)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.ticket_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = tickets.size

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bind(tickets[position], listener)
    }

    class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer{

        fun bind(ticket: Ticket, listener: OnTicketClickListener){
            ticket_name.text = ticket.name
            ticket_price.text = "$${ticket.price}"
            ticket_desription.text = ticket.description
            itemView.setOnClickListener { listener.onTicketClick(ticket) }
        }

    }
}