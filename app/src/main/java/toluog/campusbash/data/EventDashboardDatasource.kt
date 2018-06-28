package toluog.campusbash.data

import android.arch.lifecycle.MutableLiveData
import android.support.v4.util.ArrayMap
import android.util.Log
import com.google.firebase.firestore.*
import org.jetbrains.anko.collections.forEachByIndex
import toluog.campusbash.model.TicketMetaData
import toluog.campusbash.model.dashboard.TicketQuantity
import toluog.campusbash.model.dashboard.UserTicket
import toluog.campusbash.utils.AppContract

class EventDashboardDatasource() {

    private val TAG = EventDashboardDatasource::class.java.simpleName

    private val tickets = arrayListOf<UserTicket>()
    private val metadatas = ArrayMap<String, TicketMetaData>()

    private val liveTickets = MutableLiveData<List<UserTicket>>()
    private val liveMetaDatas = MutableLiveData<Map<String, TicketMetaData>>()


    fun initListener(mFirestore: FirebaseFirestore, eventId: String){
        val query = mFirestore.collection(AppContract.FIREBASE_EVENTS).document(eventId)
                .collection(AppContract.FIREBASE_EVENT_TICKET)
        query.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.d(TAG, "onEvent:error", e)
                return@EventListener
            }

            for (change in value.documentChanges) {
                if(!change.document.exists()) continue
                // Snapshot of the changed document
                Log.d(TAG, change.document.toString())
                val document = change.document
                processDocument(document, change.type)
            }

        })
    }

    private fun processDocument(doc: QueryDocumentSnapshot, type: DocumentChange.Type) {
        Log.d(TAG, doc.toString())
        val codes = doc[TICKET_CODES] as List<HashMap<String, Any>>?
        val total = (doc.data[BREAKDOWN] as HashMap<String, Long?>)[TOTAL]
        Log.d(TAG, "$codes")
        val userTicket = UserTicket()
        codes?.forEach {
            Log.d(TAG, it.toString())
            val code = mapToTicketMetadata(it, doc.id)
            if(type == DocumentChange.Type.ADDED || type == DocumentChange.Type.MODIFIED) {
                metadatas[code.code] = code
            } else {
                metadatas.remove(code.code)
            }
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
            totalPrice = total?.toDouble() ?: 0.0
        }
        if(type == DocumentChange.Type.ADDED) {
            tickets.add(userTicket)
        } else if (type == DocumentChange.Type.MODIFIED) {
            var index = -1
            for (i in 0 until tickets.size) {
                val u = tickets[i]
                if(u.ticketPurchaseId == doc.id) {
                    index = i
                    break
                }
            }
            if(index != -1) {
                tickets[index] = userTicket
            }
        } else {
            tickets.remove(userTicket)
        }
        Log.d(TAG, "$tickets")

        liveTickets.postValue(tickets)
        liveMetaDatas.postValue(metadatas)
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

    companion object {
        const val CODE = "code"
        const val QR_URL = "qrUrl"
        const val IS_USED = "isUsed"
        const val TICKET_NAME = "ticketName"
        const val TICKET_CODES = "ticketCodes"
        const val TOTAL = "totalFee"
        const val BREAKDOWN = "breakdown"
    }

}