package toluog.campusbash.data

import android.arch.lifecycle.MutableLiveData
import android.support.v4.util.ArrayMap
import android.util.Log
import com.google.firebase.firestore.*
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
        val codes = doc["ticketCodes"] as List<HashMap<String, Any>>?
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
        }
        tickets.add(userTicket)
        Log.d(TAG, "$tickets")

        liveTickets.postValue(tickets)
        liveMetaDatas.postValue(metadatas)
    }

    private fun mapToTicketMetadata(map: HashMap<String, Any>, id: String): TicketMetaData {
        return TicketMetaData().apply {
            code = map["code"] as String? ?: ""
            qrUrl = map["qrUrl"] as String? ?: ""
            isUsed = map["isUSed"] as Boolean? ?: false
            ticketName = map["ticketName"] as String? ?: ""
            ticketPurchaseId = id
        }
    }

    fun getTickets() = liveTickets

    fun getMetadatas() = liveMetaDatas

}