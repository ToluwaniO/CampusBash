package toluog.campusbash.adapters

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.ticket_quantity_item_layout.*
import org.jetbrains.anko.selector
import org.jetbrains.anko.textColor
import toluog.campusbash.R
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import org.jetbrains.anko.support.v4.selector
import toluog.campusbash.R.string.currency

/**
 * Created by oguns on 12/25/2017.
 */
class TicketAdapter(private val tickets: ArrayList<Ticket>, val context: Context): RecyclerView.Adapter<TicketAdapter.ViewHolder>() {

    private val listener: OnTicketClickListener
    private val queryMap = ArrayMap<String, Any>()
    private val priceMap = ArrayMap<String, Double>()
    var total = 0.0
    private set

    init {
        listener = context as OnTicketClickListener
    }

    interface OnTicketClickListener {
        fun onTicketClick(ticket: Ticket)
        fun onTicketQuantityChanged(queryMap: ArrayMap<String, Any>)
        fun onTotalChanged(total: Double)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ticket_quantity_item_layout,
                parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = tickets.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tickets[position], listener)
    }

    fun getPurchaseMap() = queryMap

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
            LayoutContainer{

        fun bind(ticket: Ticket, listener: OnTicketClickListener){
            val quantityLeft = ticket.quantity - ticket.ticketsSold
            val context = containerView.context
            ticket_name.text = ticket.name
            ticket_price.text = context.getString(R.string.price_currency_value, "$", ticket.price)
            if(quantityLeft > 0) {
                if(quantityLeft > 1) {
                    ticket_quantity_left.text = context.getString(R.string.tickets_left, quantityLeft)
                } else {
                    ticket_quantity_left.text = context.getString(R.string.ticket_left)
                }
                ticket_quantity.visibility = View.VISIBLE
            } else {
                ticket_quantity_left.text = context.resources.getString(R.string.sold_out)
                ticket_quantity.visibility = View.GONE
            }
            if(quantityLeft <= 5) {
                ticket_quantity_left.textColor = ContextCompat.getColor(context, R.color.colorPrimary)
            } else {
                ticket_quantity_left.textColor = ContextCompat.getColor(context, android.R.color.black)
            }

            containerView.setOnClickListener { listener.onTicketClick(ticket) }

            val selectableQuantity = arrayListOf<String>()
            val max = Math.min(10, quantityLeft)
            for (i in 0..max) {
                val breakdown = Util.getFinalFee(i * ticket.price)
                val total = breakdown[AppContract.TOTAL_FEE]?.toDouble()
                if(total != null && total <= 999999.0) {
                    selectableQuantity.add("$i")
                }
            }

            quantity_layout.setOnClickListener {
                context.selector(context.getString(R.string.select_quantity),
                        selectableQuantity) { _, i ->
                    updateMap(selectableQuantity[i].toInt(), ticket)
                    ticket_quantity.text = selectableQuantity[i]
                }
            }
        }

        private fun updateMap(quantity: Int, ticket: Ticket) {
            when (quantity) {
                0 -> {
                    queryMap.remove(ticket.ticketId)
                    priceMap.remove(ticket.ticketId)
                }
                else -> {
                    queryMap[ticket.ticketId] = quantity
                    priceMap[ticket.ticketId] = ticket.price * quantity
                }
            }
            total = 0.0
            for (v in priceMap.values) {
                total += v
            }
            listener.onTicketQuantityChanged(queryMap)
            listener.onTotalChanged(total)
        }

    }


}