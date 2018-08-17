package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.facebook.share.internal.GameRequestValidation.validate
import toluog.campusbash.R
import kotlinx.android.synthetic.main.create_ticket_layout.*
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.yesButton
import toluog.campusbash.R.string.currency
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import java.lang.ClassCastException
import java.math.BigDecimal
import android.text.Spanned
import android.text.InputFilter
import java.util.regex.Pattern


/**
 * Created by oguns on 12/23/2017.
 */
class CreateTicketFragment: Fragment(){

    interface TicketListener{
        fun ticketComplete(ticket: Ticket?)
        fun showBreakdown(map: HashMap<String, BigDecimal>)
    }
    private val TAG = CreateTicketFragment::class.java.simpleName
    private var rootView: View? = null
    private lateinit var callback: TicketListener
    private lateinit var viewModel: CreateEventViewModel
    private val currencies = ArrayList<String>()
    private val currencySymbols = ArrayList<String>()
    private val ticketTypes = arrayListOf<String>("FREE", "PAID")
    private var isStripeActivated = false
    private var currency = ""
    private var ticketType = ""
    private lateinit var typeAdapter: ArrayAdapter<String>
    private val user = FirebaseManager.getUser()

    private val priceTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(s != null && s.toString().isNotEmpty() && !s.toString().endsWith(".")) {
                if(s.toString().toDouble() <= 950000.0) {
                    val price = s.toString().toDouble()
                    updateBuyerTotal(price)
                } else {
                    updateBuyerTotal(0.0)
                }

            } else if(s != null) {
                updateBuyerTotal(0.0)
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

    }

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

        if(user != null) {
            viewModel.getUser(user.uid).observe(activity!!, Observer {
                if(it != null) {
                    Log.d(TAG, "user -> $it")
                    val id = it[AppContract.STRIPE_ACCOUNT_ID] as String?
                    isStripeActivated = id != null
                }
            })
        }

        ticket_currency.setOnClickListener {
            selector(getString(R.string.select_currency), currencies) { _, i ->
                currency = currencySymbols[i]
                ticket_currency.text = currencies[i]
                ticket_currency.error = null
            }
        }

        typeAdapter = ArrayAdapter(activity, R.layout.text_view_layout, ticketTypes)
        type_spinner.adapter = typeAdapter
        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                ticketType = ticketTypes[position].toLowerCase()

                if (ticketType == AppContract.TYPE_FREE) {
                    price_holder.visibility = View.GONE
                    ticket_currency_title.visibility = View.GONE
                    ticket_currency.visibility = View.GONE
                } else {
                    price_holder.visibility = View.VISIBLE
                    ticket_currency_title.visibility = View.VISIBLE
                    ticket_currency.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        ticket_price.addTextChangedListener(priceTextWatcher)
        ticket_price.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(950000, 2))
        save_ticket.setOnClickListener { save() }
        see_breakdown.setOnClickListener {
            callback.showBreakdown(Util.getFinalFee(ticket_price.text.toString().toDouble()))
        }

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
            if(ticketType == AppContract.TYPE_PAID) ticket?.price = price.toDouble()
            ticket?.type = ticketType
            ticket?.isVisible = visible
            if(ticketType == AppContract.TYPE_PAID) ticket?.currency = this.currency
            else ticket?.currency = ""
            if(viewModel.selectedTicket == null && ticket != null && ticketType == AppContract.TYPE_FREE) {
                viewModel.event.tickets.add(ticket)
                callback.ticketComplete(ticket)
            } else if(!isStripeActivated && ticketType == AppContract.TYPE_PAID) {
                setupStripe()
            } else if(viewModel.selectedTicket == null && ticket != null && ticketType == AppContract.TYPE_PAID) {
                viewModel.event.tickets.add(ticket)
                callback.ticketComplete(ticket)
            } else if(viewModel.selectedTicket != null) {
                viewModel.selectedTicket = null
                callback.ticketComplete(ticket)
            }
        }
    }

    private fun validate(name: String, description: String, quantity: String, price: String, currency: String): Boolean{
        var status = true
        val errMessage = getString(R.string.field_cant_be_empty)
        val errSize = getString(R.string.field_must_be_greater_zero)
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
        if (price.isEmpty() && ticketType == AppContract.TYPE_PAID){
            ticket_price.error = errMessage
            status = false
        } else if(ticketType == AppContract.TYPE_PAID && price.toDouble() < 0){
            ticket_price.error = errSize
            status = false
        } else if(ticketType == AppContract.TYPE_PAID && price.toDouble() > 950000.0) {
            ticket_price.setText("")
            ticket_price.error = getString(R.string.max_price_ticket)
            status = false
        }
        if(!currencies.contains(currency) && ticketType == AppContract.TYPE_PAID) {
            ticket_currency.error = getString(R.string.select_currency)
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

    private fun setupStripe() {
        val dialog = alert(R.string.account_not_setup_payments) {
            positiveButton(getString(R.string.yes)) {
                startActivity(intentFor<StripeSetupActivity>())
            }
            negativeButton(getString(R.string.no)) {
                it.dismiss()
            }
        }
        dialog.show()
    }

    private fun updateBuyerTotal(price: Double) {
        if(price > 0.0) {
            val priceMap = Util.getFinalFee(price)
            buyer_total.text = getString(R.string.buyer_total, priceMap[AppContract.TOTAL_FEE].toString())
            buyer_total.visibility = View.VISIBLE
            see_breakdown.visibility = View.VISIBLE
        } else {
            buyer_total.text = ""
            buyer_total.visibility = View.GONE
            see_breakdown.visibility = View.GONE
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

    inner class DecimalDigitsInputFilter(val maxDigitsBeforeDecimalPoint: Int, val maxDigitsAfterDecimalPoint: Int) : InputFilter {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            val builder = StringBuilder(dest)
            builder.replace(dstart, dend, source
                    .subSequence(start, end).toString())
            return if (!builder.toString().matches(("(([1-9]{1})([0-9]{0," +
                            (maxDigitsBeforeDecimalPoint - 1) + "})?)?(\\.[0-9]{0," +
                            maxDigitsAfterDecimalPoint + "})?").toRegex())) {
                if (source.isEmpty()) dest.subSequence(dstart, dend) else ""
            } else null

        }

    }
}