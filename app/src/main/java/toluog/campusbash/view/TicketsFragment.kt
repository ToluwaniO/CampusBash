package toluog.campusbash.view

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_tickets_layout.*
import kotlinx.android.synthetic.main.tickets_fragment.*

import toluog.campusbash.R
import toluog.campusbash.adapters.BoughtTicketAdapter
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.view.viewmodel.TicketsViewModel

class TicketsFragment : Fragment() {

    companion object {
        fun newInstance() = TicketsFragment()
    }

    private lateinit var viewModel: TicketsViewModel
    private lateinit var adapter: BoughtTicketAdapter
    private var tickets = arrayListOf<BoughtTicket>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tickets_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setNestedScrollingEnabled(tickets_recycler, false)
        adapter = BoughtTicketAdapter(tickets, context)
        viewModel = ViewModelProviders.of(this).get(TicketsViewModel::class.java)

        tickets_recycler.adapter = adapter
        tickets_recycler.layoutManager = LinearLayoutManager(context)

        viewModel.getTickets()?.observe(this, Observer {
            if(it != null) {
                tickets.clear()
                tickets.addAll(it.sortedWith(compareBy {it.timeSpent}).reversed())
                adapter.notifyDataSetChanged()
                updateView()
            }
        })
    }

    private fun updateView() {
        if(tickets.isNotEmpty()) {
            no_tickets_layout.visibility = View.GONE
            tickets_recycler.visibility = View.VISIBLE
        } else {
            no_tickets_layout.visibility = View.VISIBLE
            tickets_recycler.visibility = View.GONE
        }
    }

}
