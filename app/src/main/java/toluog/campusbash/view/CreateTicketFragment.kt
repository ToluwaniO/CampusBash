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
import toluog.campusbash.model.Ticket
import java.lang.ClassCastException

/**
 * Created by oguns on 12/23/2017.
 */
class CreateTicketFragment: Fragment(){

    interface TicketListener{
        fun ticketComplete(ticket: Ticket)
    }
    val TAG = CreateEventFragment::class.java.simpleName
    var rootView: View? = null
    lateinit var callback: TicketListener
    private lateinit var viewModel: CreateEventViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.create_ticket_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity).get(CreateEventViewModel::class.java)
        save_ticket.setOnClickListener { save() }
    }

    fun save(){
        val ticket = Ticket()
        val name = ticket_name.text.toString()
        val description = ticket_description.text.toString()
        val quantity = ticket_quantity.text.toString()
        val price = ticket_price.text.toString()

        if(validate(name, description, quantity, price)){
            ticket.name = name
            ticket.description = description
            ticket.quantity = quantity.toInt()
            ticket.price = price.toDouble()
            callback.ticketComplete(ticket)
        }
    }

    fun validate(name: String, description: String, quantity: String, price: String): Boolean{
        var status = true
        val err_message = "Field can't be empty"
        val err_size = "Field must be greater than zero"
        if(TextUtils.isEmpty(name)){
            ticket_name.error = err_message
            status = false
        }
        if(TextUtils.isEmpty(description)){
            ticket_name.error = err_message
            status = false
        }
        if (TextUtils.isEmpty(quantity)){
            ticket_quantity.error = err_message
            status = false
        } else if(quantity.toInt() < 0){
            ticket_quantity.error = err_size
        }
        if (TextUtils.isEmpty(price)){
            ticket_price.error = err_message
            status = false
        } else if(quantity.toInt() < 0){
            ticket_price.error = err_size
        }
        return status
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