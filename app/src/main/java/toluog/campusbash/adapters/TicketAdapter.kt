package toluog.campusbash.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.EditText
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.ticket_quantity_item_layout.*
import org.jetbrains.anko.textColor
import toluog.campusbash.R
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/25/2017.
 */
class TicketAdapter(private val tickets: ArrayList<Ticket>, val context: Context): RecyclerView.Adapter<TicketAdapter.ViewHolder>() {

    private val listener: OnTicketClickListener
    private val queryMap = ArrayMap<String, Any>()

    init {
        listener = context as OnTicketClickListener
    }

    interface OnTicketClickListener {
        fun onTicketClick(ticket: Ticket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ticket_quantity_item_layout, parent, false)
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
            ticket_price.text = "$${ticket.price}"
            if(quantityLeft > 0) {
                if(quantityLeft > 1) {
                    ticket_quantity_left.text = "$quantityLeft tickets left"
                } else {
                    ticket_quantity_left.text = "$quantityLeft ticket left"
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
            ticket_quantity.addTextChangedListener(TicketWatcher(ticket.name, quantityLeft,
                    ticket_quantity, queryMap))
        }

    }

    class TicketWatcher(var name: String, private var quantityLeft: Int, var view: EditText,
                        private val queryMap: ArrayMap<String, Any>): TextWatcher {

        private val TAG = TicketWatcher::class.java.simpleName

        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            view.error = null
            if(s != null) {
                val quantity = if(s.isNotEmpty()) {
                    s.toString().toInt()
                } else {
                    0
                }
                when {
                    quantity > quantityLeft -> {
                        view.error = "$quantityLeft available"
                        queryMap.remove(name)
                    }
                    quantity > 0 -> queryMap[name] = quantity
                    quantity == 0 -> queryMap.remove(name)
                    else -> {
                        view.error = "Quantity can not be negative"
                        queryMap.remove(name)
                    }
                }
            }
            Log.d(TAG, "TextChanged -> $queryMap")
        }

    }
}