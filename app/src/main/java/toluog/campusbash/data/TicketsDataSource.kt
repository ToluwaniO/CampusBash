package toluog.campusbash.data

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.*
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.utils.AppContract

object TicketsDataSource {
    private val liveTickets = MutableLiveData<List<BoughtTicket>>()
    private val tickets = arrayListOf<BoughtTicket>()
    private var listenerRegistration: ListenerRegistration? = null
    private var lastUid: String? = null
    private val TAG = TicketsDataSource::class.java.simpleName

    fun initListener(mFirestore: FirebaseFirestore, uid: String){
        Log.d(TAG, "initListener")
        if(lastUid != uid) {
            tickets.clear()
            listenerRegistration?.remove()
            val query = mFirestore.collection(AppContract.FIREBASE_USER_TICKETS)
            listenerRegistration = query.whereEqualTo(AppContract.BUYER_ID, uid)
                .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                    if (e != null) {
                        Log.d(TAG, "onEvent:error", e)
                        return@EventListener
                    }

                    // Dispatch the event
                    value?.documentChanges?.forEach {
                        if (it.document.exists() && validate(it)) {
                            // Snapshot of the changed document
                            Log.d(TAG, it.document.toString())
                            val snapshot = it.document.toObject(BoughtTicket::class.java)

                            when (it.type) {
                                DocumentChange.Type.ADDED -> {
                                    Log.d(TAG, "ChildAdded")
                                    tickets.add(snapshot)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    Log.d(TAG, "ChildModified")
                                    val index = indexOf(snapshot)
                                    if (index >= 0) {
                                        tickets[index] = snapshot
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    Log.d(TAG, "ChildRemoved")
                                    tickets.remove(snapshot)
                                }
                            }
                        }
                    }
                    liveTickets.postValue(tickets)
                })
            lastUid = uid
        }
    }

    fun getTickets() = liveTickets

    private fun indexOf(ticket: BoughtTicket): Int {
        for (i in 0 until tickets.size) {
            if(tickets[i].ticketId == ticket.ticketId) return i
        }
        return -1
    }

    private fun validate(snap: DocumentChange): Boolean {
        val doc = snap.document
        if(doc["ticketId"] == null) return false
        if(doc["buyerId"] == null) return false
        if(doc["eventId"] == null) return false
        if(doc["eventName"] == null) return false
        if(doc["eventTime"] == null) return false
        if(doc["placeholderImage"] == null) return false
        if(doc["currency"] == null) return false
        if(doc["quantity"] == null) return false
        if(doc["ticketCodes"] == null) return false
        if(doc["timeSpent"] == null) return false
        return true
    }
}