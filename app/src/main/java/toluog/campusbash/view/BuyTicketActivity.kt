package toluog.campusbash.view

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.widget.*
import android.util.Log
import kotlinx.android.synthetic.main.activity_buy_ticket.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.longToast
import toluog.campusbash.BuildConfig
import toluog.campusbash.R
import toluog.campusbash.R.string.total
import toluog.campusbash.adapters.TicketAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager


class BuyTicketActivity : AppCompatActivity(), TicketAdapter.OnTicketClickListener {

    private lateinit var eventId: String
    private val tickets: ArrayList<Ticket> = ArrayList()
    private lateinit var adapter: TicketAdapter
    private val fbaseManager = FirebaseManager()
    private var event: Event? = null
    private val TAG = BuyTicketActivity::class.java.simpleName
    private val TOKEN_REQUEST = 3456
    private var tokenId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_ticket)

        updateUi()
        
        val bundle = intent.extras
        eventId = bundle.getString(AppContract.MY_EVENT_BUNDLE)
        val viewModel: ViewEventViewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)
        viewModel.getEvent(eventId)?.observe(this, Observer { event ->
            if(event != null) {
                this.event = event
                Log.d(TAG, "$event")
                tickets.clear()
                event.tickets.filter { it.isVisible }.forEach { tickets.add(it) }
                adapter.notifyDataSetChanged()
            }
        })
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
            val price = purchase["total"] as Double
            val currency = purchase["currency"] as String?
            if(price > 0 && currency != null) {
                val cardPaymentIntent = Intent(this, CardPaymentActivity::class.java)
                cardPaymentIntent.putExtras(Bundle().apply {
                    putString("currency", currency)
                })
                startActivityForResult(cardPaymentIntent, TOKEN_REQUEST)
            } else {
                buyTickets(tokenId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if(requestCode == TOKEN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                tokenId = data.extras.getString(AppContract.TOKEN_ID)
                buyTickets(tokenId)
            }
        }
    }

    override fun onTicketClick(ticket: Ticket) {
        alert(ticket.description, ticket.name).show()
    }

    override fun onTicketQuantityChanged(queryMap: ArrayMap<String, Any>) {
        val total = getData()["total"] as Double
        total_text.text = "$$total"
    }

    private fun getData(): HashMap<String, Any> {
        val map: ArrayMap<String, Any> = adapter.getPurchaseMap()
        val purchaseMap = HashMap<String, Any>()
        var totalQuantity = 0
        var totalPrice = 0.0
        for (key in map.keys) {
            val quantity = map[key] as Int
            totalQuantity += quantity
            totalPrice += getPriceFromName(key) * quantity
        }
        val currency = getCurrency()
        if(currency != null) {
            purchaseMap["currency"] = currency
        }
        purchaseMap["debug"] = BuildConfig.DEBUG
        purchaseMap["tickets"] = map
        purchaseMap["quantity"] = totalQuantity
        purchaseMap["total"] = totalPrice
        return purchaseMap
    }

    private fun saveData(map: Map<String, Any>){
        val task = fbaseManager.buyTicket(event, map)

        task?.addOnSuccessListener {
            longToast("Ticket purchased")
            finish()
        }?.addOnFailureListener {
            longToast("Could not purchase ticket")
            Log.d(TAG, "Error saving data\nerror -> ${it.message}")
            finish()
        }
    }

    private fun  buyTickets(tokenId: String?) {
        val overallMap = getData()
        if(tokenId != null) overallMap["token"] = tokenId
        if(overallMap["quantity"] == 0){
            snackbar(container,"No ticket purchased")
        } else{
            overallMap["timeSpent"] = System.currentTimeMillis()

            val uid = FirebaseManager.auth.currentUser?.uid
            val email = FirebaseManager.auth.currentUser?.email
            val stripeId = event?.creator?.stripeAccountId
            if(stripeId != null) overallMap["stripeAccountId"] = stripeId
            if(email != null) overallMap["buyerEmail"] = email

            if(uid != null) {
                overallMap["buyerId"] = uid
                saveData(overallMap)
            } else {
                snackbar(container, "you're not signed in")
            }
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
                if(i.type == "paid") return i.currency
            }
        }
        return null
    }
}
