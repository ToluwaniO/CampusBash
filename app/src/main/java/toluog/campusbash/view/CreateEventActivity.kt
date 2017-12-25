package toluog.campusbash.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager

class CreateEventActivity : AppCompatActivity(), CreateEventFragment.CreateEventFragmentInterface, CreateTicketFragment.TicketListener {

    private val TAG = MainActivity::class.java.simpleName
    private val fragManager = supportFragmentManager
    val tickets = ArrayList<Ticket>()
    lateinit var fbaseManager: FirebaseManager
    val createEvent = CreateEventFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        fbaseManager = FirebaseManager()
        fragManager.beginTransaction().replace(R.id.fragment_frame, createEvent, AppContract.CREATE_EVENT_TAG)
                .commit()
    }

    override fun eventSaved(event: Event) {
        event.tickets = this.tickets
        fbaseManager.addEvent(event)
        finish()
    }

    override fun createTicket() {
        fragManager.beginTransaction().replace(R.id.fragment_frame, CreateTicketFragment()).commit()
    }

    override fun ticketComplete(ticket: Ticket) {
        tickets.add(ticket)
        fragManager.beginTransaction().replace(R.id.fragment_frame, createEvent).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val frags = supportFragmentManager.fragments

        for (i in frags){
            if(i is CreateEventFragment){
                i.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
