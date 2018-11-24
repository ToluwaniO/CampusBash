package toluog.campusbash.view

import android.Manifest
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Vibrator
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.collection.ArrayMap
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_scanner.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import toluog.campusbash.R
import toluog.campusbash.model.TicketMetaData
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.view.viewmodel.EventDashboardViewModel

class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val TAG = ScannerActivity::class.java.simpleName
    private lateinit var viewModel: EventDashboardViewModel
    private var permissionExplained = false

    private lateinit var eventId: String
    private val ticketMap = ArrayMap<String, TicketMetaData>()
    private val fbManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        checkPermissions()

        eventId = intent.extras.getString(AppContract.EVENT_ID)
        viewModel = ViewModelProviders.of(this).get(EventDashboardViewModel::class.java)

        viewModel.getTicketMetadatas(eventId)?.observe(this, Observer {
            Log.d(TAG, "Data changed\n$it")
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish()
                }
                return
            }
        }
    }

    override fun handleResult(result: Result?) {
        val ticketCode = result?.text ?: ""
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(500)
        }
        if(ticketMap.containsKey(ticketCode) && ticketMap[ticketCode]?.isUsed == false) {
            ticketMap[ticketCode]?.isUsed = true
            updateTicketStatusView(TicketState.VALID)
            updateTicket(ticketCode)
        } else if(ticketMap.containsKey(ticketCode) && ticketMap[ticketCode]?.isUsed == true) {
            updateTicketStatusView(TicketState.USED)
        } else {
            updateTicketStatusView(TicketState.INVALID)
        }
        doAsync {
            Thread.sleep(1000)
            uiThread {
                resumePreview()
            }
        }
    }

    private fun resumePreview() = scanner_view.resumeCameraPreview(this)

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
            val result = viewModel.updateTicket(fbManager, eventId, ticketMetaData.ticketPurchaseId,
                    "ticketCodes", true, code)
            Log.d(TAG, "sent request")
            result?.addOnSuccessListener {
                Log.d(TAG, "Field update successful")
            }?.addOnFailureListener {
                Log.d(TAG, it.toString())
            }
        }
    }

    private fun checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this@ScannerActivity,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (shouldExplainPermission()) {
                Log.d(TAG, "Requesting to show permission explanation")
                showExplanation()
            } else {
                // No explanation needed, we can request the permission.
                Log.d(TAG, "Requesting permission")
                ActivityCompat.requestPermissions(this@ScannerActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION)
            }
        } else {
            Log.d(TAG, "Permission already granted")
        }
    }

    private fun shouldExplainPermission(): Boolean {
        if (!permissionExplained && ActivityCompat.shouldShowRequestPermissionRationale(this@ScannerActivity,
                        Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this@ScannerActivity,
                        Manifest.permission.VIBRATE)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            return true
        }
        return false
    }

    private fun showExplanation() {
        permissionExplained = true
        alert(R.string.camera_vibrate_permission) {
            positiveButton(R.string.yes) {
                checkPermissions()
            }
            negativeButton(R.string.no) {
                finish()
            }
        }.show()
    }

    enum class TicketState {
        VALID, INVALID, USED
    }

    companion object {
        private const val CAMERA_PERMISSION = 1213
        private const val VIBRATE_PERMISSION = 3822
    }

}
