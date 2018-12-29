package toluog.campusbash.view

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_buy_ticket.*
import kotlinx.coroutines.launch
import toluog.campusbash.R
import toluog.campusbash.adapters.TicketAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.extension.*
import toluog.campusbash.view.viewmodel.ViewEventViewModel

class BuyTicketFragment: BaseFragment() {

    private lateinit var eventId: String
    private val tickets: ArrayList<Ticket> = ArrayList()
    private lateinit var adapter: TicketAdapter
    private var event: Event? = null
    private val TAG = BuyTicketActivity::class.java.simpleName
    private var tokenId: String? = null
    private var user: LiveData<Map<String, Any>>? = null
    private lateinit var pleaseWait: ProgressDialog
    private lateinit var viewModel: ViewEventViewModel

    private val listener = object : TicketAdapter.OnTicketClickListener {
        override fun onTicketClick(ticket: Ticket) {
            act.alertDialog(ticket.description, ticket.name).show()
        }

        override fun onTicketQuantityChanged(queryMap: ArrayMap<String, Any>) {
            viewModel.updateQuantityMap(queryMap)
        }

        override fun onTotalChanged(total: Double) {
            ticket_fee_text.text = getString(R.string.price_value, viewModel.currency, total)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)

        if(!Util.isConnected(act)) {
            startActivity(intentFor<NoNetworkActivity>())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buy_ticket, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(ViewEventViewModel::class.java)

        updateUi()

        eventId = arguments?.getString(AppContract.EVENT_ID) ?: ""
        viewModel.getTickets(eventId).observe(this, Observer { tickets ->
            if (tickets != null) {
                for (t in tickets) {
                    if (t.currency.isNotBlank()) {
                        viewModel.currency = t.currency
                        break
                    }
                }
                this.tickets.clear()
                tickets.filter { it.isVisible }.forEach { this.tickets.add(it) }
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.getEvent(eventId)?.observe(this, Observer { event ->
            if(event != null) {
                this.event = event
                viewModel.event = event
                Log.d(TAG, "$event")
            }
        })
        user = viewModel.getUser()
        pleaseWait = act.indeterminateProgressDialog(R.string.please_wait)
        pleaseWait.dismiss()

    }

    override fun onDestroyView() {
        pleaseWait.dismiss()
        super.onDestroyView()
    }

    private fun updateUi(){
        adapter = TicketAdapter(tickets, listener)
        val lManager : RecyclerView.LayoutManager = LinearLayoutManager(act)
        val dividerItemDecoration = DividerItemDecoration(act, LinearLayoutManager.VERTICAL)
        tickets_recycler.apply {
            this.layoutManager = lManager
            this.itemAnimator = DefaultItemAnimator()
            this.adapter = this@BuyTicketFragment.adapter
            addItemDecoration(dividerItemDecoration)
        }

        tickets_buy_button.setOnClickListener {
            val purchase = viewModel.getData()
            val price = adapter.total
            val currency = purchase[AppContract.CURRENCY] as String?
            if(price > 0 && currency != null) {
                val direction = BuyTicketFragmentDirections.actionBuyTicketDestinationToCardPaymentDestination()
                direction.setPrice((price*100).toInt())
                findNavController().navigate(direction)
            } else {
                buyTickets(tokenId, false)
            }
        }
    }

    private fun buyTickets(tokenId: String?, newCard: Boolean) {
        pleaseWait.show()
        this.launch {
            val state = viewModel.buyTickets(tokenId, newCard)
            pleaseWait.dismiss()
            when (state) {
                is ViewEventViewModel.BuyTicketState.Success -> {
                    adapter.getPurchaseMap().clear()
                    findNavController().navigate(BuyTicketFragmentDirections.actionBuyTicketDestinationToTicketPurchaseSuccessFragment())
                }
                is ViewEventViewModel.BuyTicketState.NotSignedIn -> {
                    Util.startSignInActivity(act)
                }
                is ViewEventViewModel.BuyTicketState.QuantityIsZero -> {
                    container.snackbar(R.string.no_ticket_purchased)
                }
                is ViewEventViewModel.BuyTicketState.Error -> {
                    container.snackbar(R.string.error_occurred)
                }
                else -> {

                }
            }
        }
    }

}