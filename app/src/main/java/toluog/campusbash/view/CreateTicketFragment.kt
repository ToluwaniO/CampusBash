package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import toluog.campusbash.R
import kotlinx.android.synthetic.main.create_ticket_layout.*
import org.jetbrains.anko.support.v4.selector
import toluog.campusbash.model.Ticket
import java.lang.ClassCastException



/**
 * Created by oguns on 12/23/2017.
 */
class CreateTicketFragment: Fragment(){

    interface TicketListener{
        fun ticketComplete(ticket: Ticket?)
    }
    private val TAG = CreateTicketFragment::class.java.simpleName
    private var rootView: View? = null
    private lateinit var callback: TicketListener
    private lateinit var viewModel: CreateEventViewModel
    private val currencies = ArrayList<String>()
    private val currencySymbols = ArrayList<String>()
    private val ticketTypes = arrayListOf<String>("FREE", "PAID")
    private var currency = ""
    private var ticketType = ""
    private lateinit var typeAdapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.create_ticket_layout, container, false)
        viewModel = ViewModelProviders.of(activity!!).get(CreateEventViewModel::class.java)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCurrencies()?.observe(this, Observer {
            currencies.clear()
            currencySymbols.clear()
            Log.d(TAG, "Currency change")
            it?.forEach {x ->
                currencies.add("${x.name} (${x.symbol})")
                currencySymbols.add(x.symbol)
                Log.d(TAG, "$x")
            }
        })
        ticket_currency.setOnClickListener {
            selector("Select currency", currencies, { _, i ->
                currency = currencySymbols[i]
                ticket_currency.text = "${currencies[i]}"
                ticket_currency.error = null
            })
        }

        typeAdapter = ArrayAdapter(activity, R.layout.text_view_layout, ticketTypes)
        type_spinner.adapter = typeAdapter
        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                ticketType = ticketTypes[position].toLowerCase()

                if (ticketType == "free") {
                    ticket_price.visibility = View.GONE
                    ticket_currency_title.visibility = View.GONE
                    ticket_currency.visibility = View.GONE
                } else {
                    ticket_price.visibility = View.VISIBLE
                    ticket_currency_title.visibility = View.VISIBLE
                    ticket_currency.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        save_ticket.setOnClickListener { save() }

        if(viewModel.selectedTicket != null) {
            updateView(viewModel.selectedTicket)
        }
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
        val visible = ticket_visibility.isChecked
        val currency = ticket_currency.text.toString().trim()

        if(validate(name, description, quantity, price, currency)){
            ticket?.name = name
            ticket?.description = description
            ticket?.quantity = quantity.toInt()
            if(ticketType == "paid") ticket?.price = price.toDouble()
            ticket?.type = ticketType
            ticket?.isVisible = visible
            if(ticketType == "paid") ticket?.currency = this.currency
            else ticket?.currency = ""
            if(viewModel.selectedTicket == null && ticket != null) {
                viewModel.event.tickets.add(ticket)
            }
            callback.ticketComplete(ticket)
        }
    }

    private fun validate(name: String, description: String, quantity: String, price: String, currency: String): Boolean{
        var status = true
        val errMessage = "Field can't be empty"
        val errSize = "Field must be greater than zero"
        if(name.isEmpty()){
            ticket_name.error = errMessage
            status = false
        }
        if(description.isEmpty()){
            ticket_description.error = errMessage
            status = false
        }
        if (quantity.isEmpty()){
            ticket_quantity.error = errMessage
            status = false
        } else if(quantity.toInt() < 0){
            ticket_quantity.error = errSize
        }
        if (price.isEmpty() && ticketType == "paid"){
            ticket_price.error = errMessage
            status = false
        } else if(ticketType == "paid" && price.toInt() < 0){
            ticket_price.error = errSize
            status = false
        }
        if(!currencies.contains(currency) && ticketType == "paid") {
            ticket_currency.error = "Choose currency"
            status = false
        }
        return status
    }

    private fun updateView(ticket: Ticket?) {
        ticket?.let {
            ticket_name.setText(ticket.name)
            ticket_description.setText(ticket.description)
            ticket_quantity.setText(ticket.quantity.toString())
            ticket_price.setText(ticket.price.toString())
            val typeIndex = ticketTypes.indexOf(ticket.type.toUpperCase())
            type_spinner.setSelection(typeIndex)
            ticket_visibility.isChecked = ticket.isVisible
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