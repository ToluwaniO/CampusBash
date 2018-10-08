package toluog.campusbash.data.datasource

import android.arch.lifecycle.MutableLiveData
import android.support.v4.util.ArrayMap
import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.coroutines.*
import toluog.campusbash.model.TicketMetaData
import toluog.campusbash.model.dashboard.TicketQuantity
import toluog.campusbash.model.dashboard.UserTicket
import toluog.campusbash.utils.AppContract
import java.util.concurrent.Executors

class EventDashboardDataSource: DataSource {

    private val TAG = EventDashboardDataSource::class.java.simpleName
    private val firestore = FirebaseFirestore.getInstance()
    private val liveTickets = MutableLiveData<List<UserTicket>>()
    private val liveMetaDatas = MutableLiveData<Map<String, TicketMetaData>>()
    private var listener: ListenerRegistration? = null
    private val threadJob = Job()
    private val threadScope = CoroutineScope(threadJob)

    fun initListener(eventId: String){
        listener?.remove()
        val query = firestore.collection(AppContract.FIREBASE_EVENTS).document(eventId)
                .collection(AppContract.FIREBASE_EVENT_TICKET)
        listener = query.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.d(TAG, "onEvent:error", e)
                return@EventListener
            }

            threadScope.launch {
                val tickets = arrayListOf<UserTicket>()
                val metadatas = ArrayMap<String, TicketMetaData>()
                value?.documents?.forEach {
                    if(it.exists()) {
                        // Snapshot of the changed document
                        Log.d(TAG, it.toString())
                        processDocument(it, tickets, metadatas)
                    }
                }
                liveTickets.postValue(tickets)
                liveMetaDatas.postValue(metadatas)
            }

        })
    }

    private suspend fun processDocument(doc: DocumentSnapshot, tickets: ArrayList<UserTicket>,
                                        metadatas: ArrayMap<String, TicketMetaData>) {
        Log.d(TAG, doc.toString())
        val codes = doc[TICKET_CODES] as List<HashMap<String, Any>>?
        val total = (doc[BREAKDOWN] as HashMap<String, Any?>)[TOTAL]
        Log.d(TAG, "$codes")
        val userTicket = UserTicket()
        codes?.forEach {
            Log.d(TAG, it.toString())
            val code = mapToTicketMetadata(it, doc.id)
            metadatas[code.code] = code
        }
        Log.d(TAG, "$metadatas")

        val quantities = doc[AppContract.TICKETS] as Map<String, Long>?
        quantities?.keys?.forEach {
            userTicket.quantities.add(TicketQuantity(it, quantities[it] ?: 0))
        }

        userTicket.apply {
            buyerEmail = doc[AppContract.BUYER_EMAIL] as String? ?: ""
            buyerName = doc[AppContract.BUYER_NAME] as String? ?: ""
            quantity = doc[AppContract.QUANTITY] as Long? ?: 0
            ticketPurchaseId = doc.id
            totalPrice = total?.toString()?.toDouble() ?: 0.0
        }
        tickets.add(userTicket)
        Log.d(TAG, "$tickets")
    }

    private fun mapToTicketMetadata(map: HashMap<String, Any>, id: String): TicketMetaData {
        return TicketMetaData().apply {
            code = map[CODE] as String? ?: ""
            qrUrl = map[QR_URL] as String? ?: ""
            isUsed = map[IS_USED] as Boolean? ?: false
            ticketName = map[TICKET_NAME] as String? ?: ""
            ticketPurchaseId = id
        }
    }

    fun getTickets() = liveTickets

    fun getMetadatas() = liveMetaDatas
    
    override fun clear() {
        listener?.remove()
        threadJob.cancel()
    }

    private val CODE = "code"
    private val QR_URL = "qrUrl"
    private val IS_USED = "isUsed"
    private val TICKET_NAME = "ticketName"
    private val TICKET_CODES = "ticketCodes"
    private val TOTAL = "ticketFee"
    private val BREAKDOWN = "breakdown"

}