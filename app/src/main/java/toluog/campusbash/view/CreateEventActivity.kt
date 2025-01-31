package toluog.campusbash.view

import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.extension.alertDialog
import toluog.campusbash.view.viewmodel.CreateEventViewModel
import java.math.BigDecimal

class CreateEventActivity : AppCompatActivity(), CreateEventFragment.CreateEventFragmentInterface,
        CreateTicketFragment.TicketListener, ViewTicketsFragment.ViewTicketsListener {

    private val TAG = CreateEventActivity::class.java.simpleName
    private val fragManager = supportFragmentManager
    private lateinit var fbaseManager: FirebaseManager
    private val createEvent =  CreateEventFragment()
    private lateinit var viewModel: CreateEventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fbaseManager = FirebaseManager()
        viewModel = ViewModelProviders.of(this).get(CreateEventViewModel::class.java)
        val bundle = intent.extras
        if(bundle != null) {
            val event = bundle[AppContract.MY_EVENT_BUNDLE] as Event
            val tickets = bundle[AppContract.TICKETS] as ArrayList<Ticket>
            createEvent.arguments = Bundle().apply {
                putParcelable(AppContract.MY_EVENT_BUNDLE, event)
                putParcelableArrayList(AppContract.TICKETS, tickets)
            }
        }
        fragManager.beginTransaction().replace(R.id.fragment_frame, createEvent, AppContract.CREATE_EVENT_TAG)
                .addToBackStack(null).commit()
    }

    override fun eventSaved(event: Event) {
        finish()
    }

    override fun getTicketList(): ArrayList<Ticket> = viewModel.tickets

    override fun createTicket() {
        fragManager.saveFragmentInstanceState(createEvent)
        createEvent.isSaved = true
        fragManager.beginTransaction().replace(R.id.fragment_frame, ViewTicketsFragment()).commit()
    }

    override fun ticketComplete(ticket: Ticket?) {
        Util.hideKeyboard(this)
        Log.d(TAG, "${viewModel.tickets}")
        fragManager.beginTransaction().replace(R.id.fragment_frame, ViewTicketsFragment()).commit()
    }

    override fun ticketClicked(ticket: Ticket) {
        fragManager.beginTransaction().replace(R.id.fragment_frame, CreateTicketFragment()).commit()
    }

    override fun addTicket() {
        fragManager.beginTransaction().replace(R.id.fragment_frame, CreateTicketFragment()).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val frags = supportFragmentManager.fragments

        frags.filterIsInstance<CreateEventFragment>()
                .forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id){
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onBackPressed() {
        val frag = fragManager.findFragmentById(R.id.fragment_frame)

        when (frag) {
            is CreateTicketFragment -> fragManager.beginTransaction().replace(R.id.fragment_frame, ViewTicketsFragment()).commit()
            is ViewTicketsFragment -> fragManager.beginTransaction().replace(R.id.fragment_frame, createEvent, AppContract.CREATE_EVENT_TAG).commit()
            else -> closeEventCreator()
        }
    }

    override fun showBreakdown(map: HashMap<String, BigDecimal>) {
        val frag = PriceBreakDownDialogFragment()
        frag.arguments = Bundle().apply {
            putSerializable(AppContract.PRICE_BREAKDOWN, map)
        }
        frag.show(supportFragmentManager.beginTransaction(), AppContract.PRICE_BREAKDOWN)
    }

    private fun closeEventCreator() {
        val dialog = alertDialog(getString(R.string.sure_you_want_to_leave))
        dialog.positiveButton(getString(R.string.yes)) {
            finish()
        }
        dialog.negativeButton(getString(R.string.no)) {
            it.dismiss()
        }
        dialog.show()
    }
}
