package toluog.campusbash.view

import android.arch.lifecycle.ViewModelProviders
import android.content.ClipDescription
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import toluog.campusbash.R
import kotlinx.android.synthetic.main.create_ticket_layout.*
import org.w3c.dom.Text
import toluog.campusbash.R.drawable.ticket
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import java.lang.ClassCastException

/**
 * Created by oguns on 12/23/2017.
 */
class CreateTicketFragment: Fragment(){

    interface TicketListener{
        fun ticketComplete(ticket: Ticket?)
    }
    private val TAG = CreateEventFragment::class.java.simpleName
    private var rootView: View? = null
    private lateinit var callback: TicketListener
    private lateinit var viewModel: CreateEventViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.create_ticket_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity).get(CreateEventViewModel::class.java)
        //viewModel.ticket = arguments?.getParcelable<Ticket>(AppContract.TICKETS_KEY)
        if(viewModel.selectedTicket != null) {
            updateView(viewModel.selectedTicket)
        }
        save_ticket.setOnClickListener { save() }
    }

    private fun save(){
        val ticket: Ticket? = if(viewModel.selectedTicket != null) {
            viewModel.selectedTicket
        } else {
            Ticket()
        }

        val name = ticket_name.text.toString().trim()
        val description = ticket_description.text.toString().trim()
        val quantity = ticket_quantity.text.toString().trim()
        val price = ticket_price.text.toString().trim()

        if(validate(name, description, quantity, price)){
            ticket?.name = name
            ticket?.description = description
            ticket?.quantity = quantity.toInt()
            ticket?.price = price.toDouble()
            if(viewModel.selectedTicket == null && ticket != null) {
                viewModel.event.tickets.add(ticket)
            }
            callback.ticketComplete(ticket)
        }
    }

    private fun validate(name: String, description: String, quantity: String, price: String): Boolean{
        var status = true
        val errMessage = "Field can't be empty"
        val errSize = "Field must be greater than zero"
        if(TextUtils.isEmpty(name)){
            ticket_name.error = errMessage
            status = false
        }
        if(TextUtils.isEmpty(description)){
            ticket_name.error = errMessage
            status = false
        }
        if (TextUtils.isEmpty(quantity)){
            ticket_quantity.error = errMessage
            status = false
        } else if(quantity.toInt() < 0){
            ticket_quantity.error = errSize
        }
        if (TextUtils.isEmpty(price)){
            ticket_price.error = errMessage
            status = false
        } else if(quantity.toInt() < 0){
            ticket_price.error = errSize
        }
        return status
    }

    private fun updateView(ticket: Ticket?) {
        ticket?.let {
            ticket_name.setText(ticket.name)
            ticket_description.setText(ticket.description)
            ticket_quantity.setText(ticket.quantity.toString())
            ticket_price.setText(ticket.price.toString())
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            callback = context as TicketListener
        }catch (e: ClassCastException){
            Log.d(TAG, e.message)
        }
    }
}