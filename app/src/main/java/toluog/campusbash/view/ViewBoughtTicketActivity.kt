package toluog.campusbash.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_view_bought_ticket.*
import kotlinx.android.synthetic.main.ticket_card_layout.*
import toluog.campusbash.R
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.model.TicketMetaData
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.loadImage

class ViewBoughtTicketActivity : AppCompatActivity() {
    private val tickets = arrayListOf<TicketMetaData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_bought_ticket)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val codes = (intent.extras[AppContract.BOUGHT_TICKET] as BoughtTicket?)?.ticketCodes
        codes?.let { tickets.addAll(it) }
        ticket_recycler.adapter = TicketsAdapter()
        ticket_recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id){
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    inner class TicketsAdapter(): RecyclerView.Adapter<TicketsAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.ticket_card_layout, parent,
                    false)
            return ViewHolder(v)
        }

        override fun getItemCount() = tickets.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(tickets[position])
        }

        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
            fun bind(ticketMetaData: TicketMetaData) {
                qr_image.loadImage(ticketMetaData.qrUrl)
                ticket_name.text = ticketMetaData.ticketName
            }
        }

    }
}
