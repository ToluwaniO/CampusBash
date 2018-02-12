package toluog.campusbash.view

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

class CreateEventActivity : AppCompatActivity(), CreateEventFragment.CreateEventFragmentInterface, CreateTicketFragment.TicketListener {

    private val TAG = CreateEventActivity::class.java.simpleName
    private val fragManager = supportFragmentManager
    val tickets = ArrayList<Ticket>()
    lateinit var fbaseManager: FirebaseManager
    val createEvent = CreateEventFragment()
    private lateinit var viewModel: CreateEventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        fbaseManager = FirebaseManager()
        viewModel = ViewModelProviders.of(this).get(CreateEventViewModel::class.java)
        fragManager.beginTransaction().replace(R.id.fragment_frame, createEvent, AppContract.CREATE_EVENT_TAG)
                .addToBackStack(null).commit()
    }

    override fun eventSaved(event: Event) {
        finish()
    }

    override fun getTicketList(): ArrayList<Ticket> = viewModel.event.tickets

    override fun createTicket() {
        fragManager.saveFragmentInstanceState(createEvent)
        createEvent.isSaved = true
        fragManager.beginTransaction().replace(R.id.fragment_frame, CreateTicketFragment()).commit()
    }

    override fun ticketComplete(ticket: Ticket) {
        Util.hideKeyboard(this)
        viewModel.event.tickets.add(ticket)
        Log.d(TAG, "${viewModel.event.tickets}")
        fragManager.beginTransaction().replace(R.id.fragment_frame, createEvent).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val frags = supportFragmentManager.fragments

        frags.filterIsInstance<CreateEventFragment>()
                .forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun onBackPressed() {
        val frag = fragManager.findFragmentById(R.id.fragment_frame)

        if(frag is CreateTicketFragment) {
            fragManager.beginTransaction().replace(R.id.fragment_frame, createEvent).commit()
        } else {
            finish()
        }
    }
}
