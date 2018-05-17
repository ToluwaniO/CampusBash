package toluog.campusbash.view

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
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
            val event = intent.extras[AppContract.MY_EVENT_BUNDLE] as Event
            createEvent.arguments = Bundle().apply {
                putParcelable(AppContract.MY_EVENT_BUNDLE, event)
            }
        }
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
        fragManager.beginTransaction().replace(R.id.fragment_frame, ViewTicketsFragment()).commit()
    }

    override fun ticketComplete(ticket: Ticket?) {
        Util.hideKeyboard(this)
        Log.d(TAG, "${viewModel.event.tickets}")
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
        val dialog = alert(getString(R.string.sure_you_want_to_leave)) {
            yesButton { finish() }
            noButton { it.dismiss() }
        }
        dialog.show()
    }
}
