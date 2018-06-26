package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_scanner.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import toluog.campusbash.R
import toluog.campusbash.model.TicketMetaData
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager

class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val TAG = ScannerActivity::class.java.simpleName
    private lateinit var viewModel: EventDashboardViewModel

    private lateinit var eventId: String
    private val ticketMap = ArrayMap<String, TicketMetaData>()
    private val fbManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        eventId = intent.extras.getString(AppContract.EVENT_ID)
        viewModel = ViewModelProviders.of(this).get(EventDashboardViewModel::class.java)

        viewModel.getTicketMetadatas(eventId).observe(this, Observer {
            if(it != null) {
                ticketMap.clear()
                ticketMap.putAll(it)
            } else {
                ticketMap.clear()
                emoji_view.setImageResource(R.drawable.ic_sad_face_emoji)
                ticket_message_view.text = getString(R.string.no_tickets_to_scan)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        resetCamera()
    }

    override fun onPause() {
        super.onPause()
        resetCamera(false)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId

        return when(itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> false
        }
    }

    override fun handleResult(result: Result?) {
        val ticketCode = result?.text ?: ""
        if(ticketMap.containsKey(ticketCode) && ticketMap[ticketCode]?.isUsed == false) {
            updateTicketStatusView(TicketState.VALID)
            ticketMap[ticketCode]?.isUsed = true
            updateTicket(ticketCode)
        } else if(ticketMap.containsKey(ticketCode) && ticketMap[ticketCode]?.isUsed == true) {
            updateTicketStatusView(TicketState.USED)
        } else {
            updateTicketStatusView(TicketState.INVALID)
        }
        scanner_view.resumeCameraPreview(this)
    }

    private fun resetCamera(start: Boolean = true) {
        doAsync {
            if(start) {
                scanner_view.startCamera()
                uiThread {
                    scanner_view.setResultHandler(this@ScannerActivity)
                    scanner_view.visibility = View.VISIBLE
                }
            } else {
                scanner_view.stopCamera()
            }
        }
    }

    private fun updateTicketStatusView(state: TicketState) {
        when(state) {
            TicketState.VALID -> {
                emoji_view.setImageResource(R.drawable.smiling_face_emoji)
                ticket_message_view.text = getString(R.string.ticket_valid)
            }
            TicketState.USED -> {
                emoji_view.setImageResource(R.drawable.neutral_face_emoji)
                ticket_message_view.text = getString(R.string.ticket_used)
            }
            TicketState.INVALID -> {
                emoji_view.setImageResource(R.drawable.expressionless_face_emoji)
                ticket_message_view.text = getString(R.string.ticket_invalid)
            }
        }
    }

    private fun updateTicket(code: String) {
        Log.d(TAG, "updating ticket")
        val ticketMetaData = ticketMap[code] ?: TicketMetaData()
        doAsync {
            val newList = arrayListOf<Any>()
            ticketMap.values.filter {
                it.ticketPurchaseId == ticketMetaData.ticketPurchaseId
            }.flatMapTo(newList) { listOf(it.toMap()) }
            val result = viewModel.updateTicket(fbManager, eventId, ticketMetaData.ticketPurchaseId,
                    "ticketCodes", newList)
            Log.d(TAG, "sent request")
            result?.addOnSuccessListener {
                Log.d(TAG, "Field update successful")
            }?.addOnFailureListener {
                Log.d(TAG, it.toString())
            }
        }
    }

    enum class TicketState {
        VALID, INVALID, USED
    }

}
