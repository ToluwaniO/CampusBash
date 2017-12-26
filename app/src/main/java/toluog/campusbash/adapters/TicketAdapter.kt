package toluog.campusbash.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
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
     lateinit var textChangedListener: TextWatcher

    init {
        listener = context as OnTicketClickListener
        setTextListener()
    }

    interface OnTicketClickListener {
        fun onTicketClick(ticket: Ticket)
        fun onQuantityChanged(quantity: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.ticket_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = tickets.size

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bind(tickets[position], listener, textChangedListener)
    }

    private fun setTextListener(){
        textChangedListener = object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val q = s?.subSequence(0, s.lastIndex)?.toString()
                if(q != null)
                    listener.onQuantityChanged(q.toInt())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }
    }

    class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer{

        fun bind(ticket: Ticket, listener: OnTicketClickListener, textListener: TextWatcher){
            ticket_name.text = ticket.name
            ticket_price.text = "$${ticket.price}"
            ticket_desription.text = ticket.description
            //ticket_quantity.addTextChangedListener(textListener)
            itemView.setOnClickListener { listener.onTicketClick(ticket) }
        }

    }
}