package toluog.campusbash.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.bought_ticket_layout.*
import toluog.campusbash.R
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.loadImage
import java.util.*

class BoughtTicketAdapter(var tickets: List<BoughtTicket>, var context: Context?): RecyclerView.Adapter<BoughtTicketAdapter.ViewHolder>() {

    private val listener: TicketListener

    init {
        listener = context as TicketListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.bought_ticket_layout, parent,
                false)
        return ViewHolder(v)
    }

    override fun getItemCount() = tickets.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tickets[position], listener, context)
    }

    class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(ticket: BoughtTicket, listener: TicketListener, context: Context?) {
            if(ticket.ticketCodes.isNotEmpty()) {
                event_ticket.loadImage(ticket.placeholderImage?.url ?: containerView.context.getDrawable(R.drawable.default_event_background))
            }
            event_name.text = ticket.eventName
            tickets_number.text = if(ticket.ticketCodes.size > 1) {
                context?.getString(R.string.ticket_quantity_with_params, ticket.ticketCodes.size)
            } else {
                context?.getString(R.string.ticket_quantity_one)
            }
            val time = Util.formatDate(Calendar.getInstance().apply {
                timeInMillis = ticket.eventTime
            })
            bought_date.text = context?.getString(R.string.starts_on_x, time)

            containerView.setOnClickListener {
                listener.onTicketClicked(ticket)
            }
        }
    }

    interface TicketListener {
        fun onTicketClicked(ticket: BoughtTicket)
    }

}