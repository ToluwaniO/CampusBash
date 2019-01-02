package toluog.campusbash.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract

class ViewEventActivity : AppCompatActivity(), ViewEventFragment.ViewEventFragmentListener,
        BuyTicketFragment.BuyTicketFragmentListener, CardPaymentFragment.CardPaymentFragmentListener {

    private val TAG = ViewEventActivity::class.java.simpleName
    private lateinit var eventId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportPostponeEnterTransition()

        eventId = intent?.extras?.getString(AppContract.EVENT_ID) ?: return

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_frame,
                    ViewEventFragment.newInstance(eventId), ViewEventFragment::class.java.simpleName).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_frame)
        fragment?.onOptionsItemSelected(item)

        val id = item?.itemId
        when (id) {
            android.R.id.home -> handleBackNav()
        }
        return true
    }

    override fun onBackPressed() {
        handleBackNav()
    }

    private fun handleBackNav() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_frame)
        when (fragment) {
            is ViewEventFragment -> finish()
            is BuyTicketFragment -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame,
                        ViewEventFragment.newInstance(eventId), ViewEventFragment::class.java.simpleName).commit()
            }
            is CardPaymentFragment -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame,
                        BuyTicketFragment.newInstance(eventId), BuyTicketFragment::class.java.simpleName).commit()
            }
            is TicketPurchaseSuccessFragment -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_frame,
                        ViewEventFragment.newInstance(eventId), ViewEventFragment::class.java.simpleName).commit()
            }
        }
    }

    override fun getTicketClicked() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_frame,
                BuyTicketFragment.newInstance(eventId), BuyTicketFragment::class.java.simpleName).commit()
    }

    override fun getTicketClicked(price: Int) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_frame,
                CardPaymentFragment.newInstance(price), CardPaymentFragment::class.java.simpleName).commit()
    }

    override fun freeTicketsPurchased() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_frame,
                TicketPurchaseSuccessFragment(), TicketPurchaseSuccessFragment::class.java.simpleName).commit()
    }

    override fun cardPaymentCompleted() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_frame,
                TicketPurchaseSuccessFragment(), TicketPurchaseSuccessFragment::class.java.simpleName).commit()
    }

}
