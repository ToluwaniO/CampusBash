package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_buy_ticket.*
import kotlinx.android.synthetic.main.ticket_item_layout.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.longToast
import toluog.campusbash.R
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
                tickets.clear()
                for(i in event.tickets){
                    tickets.add(i)
                }
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun updateUi(){
        adapter = TicketAdapter(tickets, this)
        val layoutManager : RecyclerView.LayoutManager = GridLayoutManager(this, 1)
        tickets_recycler.layoutManager = layoutManager
        tickets_recycler.itemAnimator = DefaultItemAnimator()
        tickets_recycler.adapter = adapter

        tickets_buy_button.setOnClickListener {
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
                    if(quan != null) overallMap.put("quantity", quan)
                    dataMap.remove("quantity")
                    saveData(overallMap)
                } else {
                    snackbar(container, "you're not signed in")
                }
            }
        }
    }

    override fun onTicketClick(ticket: Ticket) {

    }

    private fun getData(): HashMap<String, Int> {
        val map = HashMap<String, Int>()
        var total = 0
        for (i in 0 until tickets.size){
            val holder = tickets_recycler.findViewHolderForAdapterPosition(i) as TicketAdapter.ViewHolder?
            if(holder != null){
                val name = tickets[i].name
                val quantityString = holder.ticket_quantity.text.toString()
                var quantity = 0
                if(!TextUtils.isEmpty(quantityString)){
                    quantity = quantityString.toInt()
                }
                map.put(name, quantity)
                total += quantity
            }
        }
        map.put("quantity", total)
        return map
    }

    private fun saveData(map: Map<String, Any>){
        val task = fbaseManager.buyTicket(event, map)

        task?.addOnSuccessListener {
            longToast("Ticket purchased")
            finish()
        }?.addOnFailureListener {
            longToast("Could not purchase ticket")
            finish()
        }
    }
}
