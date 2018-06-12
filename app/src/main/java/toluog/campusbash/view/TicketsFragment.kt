package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_tickets_layout.*
import kotlinx.android.synthetic.main.tickets_fragment.*

import toluog.campusbash.R
import toluog.campusbash.adapters.BoughtTicketAdapter
import toluog.campusbash.model.BoughtTicket

class TicketsFragment : Fragment() {

    companion object {
        fun newInstance() = TicketsFragment()
    }

    private lateinit var viewModel: TicketsViewModel
    private lateinit var adapter: BoughtTicketAdapter
    private val tickets = arrayListOf<BoughtTicket>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tickets_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = BoughtTicketAdapter(tickets)
        viewModel = ViewModelProviders.of(this).get(TicketsViewModel::class.java)

        tickets_recycler.adapter = adapter
        tickets_recycler.layoutManager = LinearLayoutManager(context)

        viewModel.getTickets()?.observe(this, Observer {
            it?.let {
                tickets.clear()
                tickets.addAll(it)
                adapter.notifyDataSetChanged()
                updateView()
            }
        })

    }

    fun updateView() {
        if(tickets.isNotEmpty()) {
            no_tickets_layout.visibility = View.GONE
            tickets_recycler.visibility = View.VISIBLE
        } else {
            no_tickets_layout.visibility = View.VISIBLE
            tickets_recycler.visibility = View.GONE
        }
    }

}
