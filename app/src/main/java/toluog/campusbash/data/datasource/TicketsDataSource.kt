package toluog.campusbash.data.datasource

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.utils.AppContract
import java.util.concurrent.Executors

class TicketsDataSource: DataSource {

    private val liveTickets = MutableLiveData<List<BoughtTicket>>()
    private val tickets = arrayListOf<BoughtTicket>()
    private val firestore = FirebaseFirestore.getInstance()
    private val threadJob = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val threadScope = CoroutineScope(threadJob)
    private var listener: ListenerRegistration? = null
    private val TAG = TicketsDataSource::class.java.simpleName

    fun initListener(uid: String){
        Log.d(TAG, "initListener")

        val query = firestore.collection(AppContract.FIREBASE_USER_TICKETS)
        listener = query.whereEqualTo(AppContract.BUYER_ID, uid)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.d(TAG, "onEvent:error", e)
                    return@EventListener
                }
                threadScope.launch {
                    val tickets = arrayListOf<BoughtTicket>()
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
                }
            })
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

    override fun clear() {
        listener?.remove()
        threadJob.cancel()
    }
}