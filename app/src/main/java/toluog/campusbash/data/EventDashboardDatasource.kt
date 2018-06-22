package toluog.campusbash.data

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.*
import toluog.campusbash.model.TicketMetaData
import toluog.campusbash.model.dashboard.TicketQuantity
import toluog.campusbash.model.dashboard.UserTicket
import toluog.campusbash.utils.AppContract

class EventDashboardDatasource() {

    private val TAG = EventDashboardDatasource::class.java.simpleName

    private val tickets = arrayListOf<UserTicket>()
    private val metadatas = HashMap<String, TicketMetaData>()

    private val liveTickets = MutableLiveData<List<UserTicket>>()
    private val liveMetaDatas = MutableLiveData<Map<String, TicketMetaData>>()


    fun initListener(mFirestore: FirebaseFirestore, eventId: String){
        val query = mFirestore.collection(AppContract.FIREBASE_EVENTS).document(eventId)
                .collection(AppContract.FIREBASE_USER_TICKETS)
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
        val codes = doc["ticketCodes"] as List<Any>?
        val userTicket = UserTicket()
        codes?.forEach {
            val code = it as TicketMetaData
            if(type == DocumentChange.Type.ADDED || type == DocumentChange.Type.MODIFIED) {
                metadatas.put(code.code, code)
            } else {
                metadatas.remove(code.code)
            }
        }

        val quantities = doc[AppContract.TICKETS] as Map<String, Int>?
        quantities?.keys?.forEach {
            userTicket.quantities.add(TicketQuantity(it, quantities[it] ?: 0))
        }

        userTicket.apply {
            buyerEmail = doc[AppContract.BUYER_EMAIL] as String? ?: ""
            buyerName = doc[AppContract.BUYER_NAME] as String? ?: ""
        }
        tickets.add(userTicket)

        liveTickets.postValue(tickets)
        liveMetaDatas.postValue(metadatas)
    }

    fun getTickets() = liveTickets

    fun getMetadatas() = liveMetaDatas

}