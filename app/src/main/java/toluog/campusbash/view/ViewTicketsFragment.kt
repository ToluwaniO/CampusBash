package toluog.campusbash.view

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.ticket_item_layout.*
import kotlinx.android.synthetic.main.view_tickets_layout.*
import toluog.campusbash.R
import toluog.campusbash.model.Ticket
import java.lang.ClassCastException
import android.support.v7.widget.DividerItemDecoration
import toluog.campusbash.utils.AppContract


/**
 * Created by oguns on 2/27/2018.
 */
class ViewTicketsFragment: Fragment() {

    private lateinit var tickets: ArrayList<Ticket>
    private lateinit var rootView: View
    private lateinit var adapter: TicketsAdapter
    private lateinit var callback: ViewTicketsListener
    private lateinit var viewModel: CreateEventViewModel
    private val TAG = ViewTicketsFragment::class.java.simpleName

    interface ViewTicketsListener {
        fun ticketClicked(ticket: Ticket)
        fun addTicket()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.view_tickets_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(CreateEventViewModel::class.java)

        tickets = viewModel.event.tickets
        adapter = TicketsAdapter()
        val layoutManager = LinearLayoutManager(rootView.context)
        tickets_recycler.layoutManager = layoutManager
        tickets_recycler.itemAnimator = DefaultItemAnimator()
        val dividerItemDecoration = DividerItemDecoration(tickets_recycler.context, layoutManager.orientation)
        tickets_recycler.addItemDecoration(dividerItemDecoration)
        tickets_recycler.adapter = adapter

        fab.setOnClickListener {
            callback.addTicket()
        }

        if(tickets.size == 0) {
            no_tickets_layout.visibility = View.VISIBLE
        } else {
            tickets_recycler.visibility = View.VISIBLE
            Log.d(TAG, "$tickets")
            adapter.notifyDataSetChanged()
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            callback = context as ViewTicketsListener
        }catch (e: ClassCastException){
            Log.d(TAG, e.message)
        }
    }

    private fun updateView() {
        if(tickets.size == 0) {
            tickets_recycler.visibility = View.GONE
            no_tickets_layout.visibility = View.VISIBLE
        } else{
            tickets_recycler.visibility = View.VISIBLE
            no_tickets_layout.visibility = View.GONE
        }
    }

    inner class TicketsAdapter : RecyclerView.Adapter<TicketsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketsAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ticket_item_layout, parent,
                    false)
            return ViewHolder(view)
        }

        override fun getItemCount() = tickets.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(tickets[position])
            Log.d(TAG, "Binding view")
        }


        inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView)
                , LayoutContainer {

            fun bind(ticket: Ticket) {
                name.text = ticket.name
                if(ticket.type == AppContract.TYPE_FREE) {
                    price.text = getString(R.string.free_caps)
                } else if(ticket.type == AppContract.TYPE_PAID) {
                    price.text = getString(R.string.price_currency_value, ticket.currency, ticket.price)
                }
                quantity.text = getString(R.string.ticket_quantity_with_params, ticket.quantity)

                delete.setOnClickListener {
                    tickets.removeAt(adapterPosition)
                    notifyDataSetChanged()
                    updateView()
                }
                containerView?.setOnClickListener {
                    viewModel.selectedTicket = ticket
                    callback.ticketClicked(ticket)
                }
            }

        }
    }
}