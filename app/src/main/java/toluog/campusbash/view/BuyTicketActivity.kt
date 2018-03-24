package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.widget.*
import android.text.TextUtils
import android.util.Log
import kotlinx.android.synthetic.main.activity_buy_ticket.*
import kotlinx.android.synthetic.main.ticket_quantity_item_layout.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.longToast
import toluog.campusbash.R
import toluog.campusbash.R.drawable.ticket
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

        tickets_buy_button.setOnClickListener { buyTickets() }
    }

    override fun onTicketClick(ticket: Ticket) {
        alert(ticket.description, ticket.name).show()
    }

    private fun getData(): ArrayMap<String, Any> {
        val map: ArrayMap<String, Any> = adapter.getPurchaseMap()
        var totalQuantity = 0
        var totalPrice = 0.0
        for (key in map.keys) {
            val quantity = map[key] as Int
            totalQuantity += quantity
            totalPrice += getPriceFromName(key) * quantity
        }
        map["quantity"] = totalQuantity
        map["total"] = totalPrice
        return map
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

    private fun buyTickets() {
        val dataMap = getData()
        val overallMap = HashMap<String, Any>()
        if(dataMap["quantity"] == 0){
            snackbar(container,"No ticket purchased")
        } else{
            overallMap.put("tickets", dataMap)
            overallMap.put("timeSpent", System.currentTimeMillis())

            val uid = FirebaseManager.auth.currentUser?.uid

            if(uid != null) {
                overallMap.put("buyerId", uid)
                val quan = dataMap["quantity"]
                val total = dataMap["total"]
                if(quan != null) overallMap.put("quantity", quan)
                if(total != null) overallMap.put("total", total)
                dataMap.remove("quantity")
                dataMap.remove("total")
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
}
