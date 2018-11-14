package toluog.campusbash.view

import android.app.Activity
import android.app.ProgressDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.widget.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_buy_ticket.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import toluog.campusbash.R
import toluog.campusbash.adapters.TicketAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.model.TicketPriceBreakdown
import toluog.campusbash.model.toMap
import toluog.campusbash.utils.*
import toluog.campusbash.view.viewmodel.ViewEventViewModel
import java.math.BigDecimal


class BuyTicketActivity : AppCompatActivity(), TicketAdapter.OnTicketClickListener {

    private lateinit var eventId: String
    private val tickets: ArrayList<Ticket> = ArrayList()
    private lateinit var adapter: TicketAdapter
    private val fbaseManager = FirebaseManager()
    private var event: Event? = null
    private val TAG = BuyTicketActivity::class.java.simpleName
    private val TOKEN_REQUEST = 3456
    private var tokenId: String? = null
    private var user: LiveData<Map<String, Any>>? = null
    private lateinit var pleaseWait: ProgressDialog
    private var currency = "$"
    private var breakdown = TicketPriceBreakdown()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_ticket)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(!Util.isConnected(this)) {
            startActivity(intentFor<NoNetworkActivity>())
            finish()
        }
        val customerId = user?.value?.get(AppContract.STRIPE_CUSTOMER_ID) as String?
        if(!CampusBash.stripeSessionStarted) {
            CampusBash.initCustomerSession(customerId, this)
        }

        updateUi()
        
        val bundle = intent.extras
        eventId = bundle?.getString(AppContract.MY_EVENT_BUNDLE) ?: ""
        val viewModel: ViewEventViewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)
        viewModel.getEvent(eventId)?.observe(this, Observer { event ->
            if(event != null) {
                for (t in event.tickets) {
                    if (t.currency.isNotBlank()) {
                        currency = t.currency
                        break
                    }
                }
                this.event = event
                Log.d(TAG, "$event")
                tickets.clear()
                event.tickets.filter { it.isVisible }.forEach { tickets.add(it) }
                adapter.notifyDataSetChanged()
            }
        })
        user = viewModel.getUser()
        pleaseWait = indeterminateProgressDialog(R.string.please_wait)
        pleaseWait.dismiss()

    }

    override fun onDestroy() {
        pleaseWait.dismiss()
        super.onDestroy()
    }

    private fun updateUi(){
        adapter = TicketAdapter(tickets, this)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        tickets_recycler.layoutManager = layoutManager
        tickets_recycler.itemAnimator = DefaultItemAnimator()
        tickets_recycler.adapter = adapter
        tickets_recycler.addItemDecoration(dividerItemDecoration)

        tickets_buy_button.setOnClickListener {
            val purchase = getData()
            val price = adapter.total
            val currency = purchase[AppContract.CURRENCY] as String?
            if(price > 0 && currency != null) {
                val cardPaymentIntent = Intent(this, CardPaymentActivity::class.java)
                cardPaymentIntent.putExtras(Bundle().apply {
                    putString(AppContract.CURRENCY, currency)
                    putInt(AppContract.BUNDLE_PRICE, (price*100).toInt())
                })
                startActivityForResult(cardPaymentIntent, TOKEN_REQUEST)
            } else {
                buyTickets(tokenId, false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == TOKEN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if(data != null) {
                    tokenId = data.extras?.getString(AppContract.TOKEN_ID) ?: ""
                    breakdown = data.extras?.getParcelable(AppContract.BREAKDOWN) ?: TicketPriceBreakdown()
                    val newCard = data.extras?.getBoolean(AppContract.NEW_CARD) ?: false
                    buyTickets(tokenId, newCard)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id){
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onTicketClick(ticket: Ticket) {
        alert(ticket.description, ticket.name).show()
    }

    override fun onTicketQuantityChanged(queryMap: ArrayMap<String, Any>) {
//        Log.d(TAG, "Ticket quantity changed")
//        val breakdown = getData()["breakdown"] as HashMap<String, Double>
//        Log.d(TAG, "Breakdown -> $breakdown")
//        val total = breakdown[AppContract.TOTAL_FEE]
//        if(total != null && total > 0) {
//            price_breakdown_layout.visibility = View.VISIBLE
//            ticket_fee_text.text = getString(R.string.price_value, "$", breakdown[AppContract.TICKET_FEE])
//            payment_fee_text.text = getString(R.string.price_value, "$", breakdown[AppContract.PAYMENT_FEE])
//            service_fee_text.text = getString(R.string.price_value, "$", breakdown[AppContract.SERVICE_FEE])
//            total_fee_text.text = getString(R.string.price_value, "$", total)
//        } else {
//            price_breakdown_layout.visibility = View.GONE
//        }
    }

    override fun onTotalChanged(total: Double) {
        ticket_fee_text.text = getString(R.string.price_value, currency, total)
    }

    private fun getData(): HashMap<String, Any> {
        val map: ArrayMap<String, Any> = adapter.getPurchaseMap()
        val purchaseMap = HashMap<String, Any>()
        var totalQuantity = 0
        for (key in map.keys) {
            val quantity = map[key] as Int
            totalQuantity += quantity
        }
        val currency = getCurrency()
        if(currency != null) {
            purchaseMap[AppContract.CURRENCY] = currency
        }
        purchaseMap[AppContract.TICKETS] = map
        purchaseMap[AppContract.QUANTITY] = totalQuantity
        purchaseMap[AppContract.TOTAL] = breakdown.totalFee
        purchaseMap[AppContract.BREAKDOWN] = breakdown.toMap()
        return purchaseMap
    }

    private fun saveData(map: Map<String, Any>){
        val task = fbaseManager.buyTicket(event, map)
        Log.d(TAG, "$map")
        task?.addOnSuccessListener {
            event?.let { ev -> Analytics.logTicketBought(ev) }
            longToast(R.string.ticket_purchased)
            finish()
        }?.addOnFailureListener {
            event?.let { ev -> Analytics.logTicketBoughtFailed(ev) }
            longToast(R.string.could_not_purchase_ticket)
            Log.e(TAG, "Error saving data\nerror -> ${it.message}")
            finish()
        }
    }

    private fun buyTickets(tokenId: String?, newCard: Boolean) {
        val overallMap = getData()
        if(overallMap[AppContract.QUANTITY] == 0){
            snackbar(container, R.string.no_ticket_purchased)
            return
        }
        pleaseWait.show()
        val customerId = user?.value?.get(AppContract.STRIPE_CUSTOMER_ID) as String?
        val userName = user?.value?.get(AppContract.FIREBASE_USER_USERNAME) as String?
        if(tokenId != null) {
            overallMap[AppContract.TOKEN] = tokenId
            overallMap[AppContract.NEW_CARD] = newCard
        }
        if(customerId != null) {
            overallMap[AppContract.STRIPE_CUSTOMER_ID] = customerId
        }
        if(userName != null) {
            overallMap[AppContract.BUYER_NAME] = userName
        }

        overallMap[AppContract.TIME_SPENT] = System.currentTimeMillis()

        val uid = FirebaseManager.auth.currentUser?.uid
        val email = FirebaseManager.auth.currentUser?.email
        val stripeId = event?.creator?.stripeAccountId
        if(stripeId != null) overallMap[AppContract.STRIPE_ACCOUNT_ID] = stripeId
        if(email != null) overallMap[AppContract.BUYER_EMAIL] = email

        if(uid != null) {
            overallMap[AppContract.BUYER_ID] = uid
            saveData(overallMap)
        } else {
            pleaseWait.dismiss()
            snackbar(container, R.string.not_signed_in)
        }
    }

    private fun getPriceFromName(name: String): Double {
        var price = 0.0
        for (t in tickets) {
            if(t.name == name) {
                price = t.price
            }
        }
        return price
    }

    private fun getCurrency(): String? {
        val tickets = event?.tickets
        if(tickets != null) {
            for (i in tickets) {
                if(i.type == AppContract.TYPE_PAID) return i.currency
            }
        }
        return null
    }

    private fun convertBigDecimalToDoubleMap(map: HashMap<String, BigDecimal>): HashMap<String, Double> {
        val temp = HashMap<String, Double>()
        for (key in map.keys) {
            val bd = map[key]
            if(bd != null) {
                temp[key] = bd.toDouble()
            }
        }
        return temp
    }

}
