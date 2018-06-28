package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.Result
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.coroutines.experimental.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import toluog.campusbash.R
import toluog.campusbash.model.TicketMetaData
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ScannerFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ScannerFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ScannerFragment : Fragment(), ZXingScannerView.ResultHandler {

    private val TAG = ScannerFragment::class.java.simpleName
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var viewModel: EventDashboardViewModel

    private lateinit var eventId: String
    private val ticketMap = ArrayMap<String, TicketMetaData>()
    private val fbManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(AppContract.EVENT_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(EventDashboardViewModel::class.java)

        viewModel.getTicketMetadatas(eventId).observe(this, Observer {
            ticketMap.clear()
            if(it != null) {
                ticketMap.putAll(it)
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

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun handleResult(result: Result?) {
        val ticketCode = result?.text ?: ""
        if(ticketMap.containsKey(ticketCode) && ticketMap[ticketCode]?.isUsed == false) {
            toast("Ticket is valid")
            ticketMap[ticketCode]?.isUsed = true
            updateTicket(ticketCode)
        } else if(ticketMap.containsKey(ticketCode) && ticketMap[ticketCode]?.isUsed == true) {
            toast("Ticket has been used")
        } else {
            toast("Ticket is invalid")
        }
        scanner_view.resumeCameraPreview(this)
    }

    private fun resetCamera(start: Boolean = true) {
        doAsync {
            if(start) {
                scanner_view.startCamera()
                uiThread {
                    scanner_view.setResultHandler(this@ScannerFragment)
                    scanner_view.visibility = View.VISIBLE
                }
            } else {
                scanner_view.stopCamera()
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param eventId Parameter 1.
         * @return A new instance of fragment ScannerFragment.
         */
        @JvmStatic
        fun newInstance(eventId: String) =
                ScannerFragment().apply {
                    arguments = Bundle().apply {
                        putString(AppContract.EVENT_ID, eventId)
                    }
                }
    }
}
