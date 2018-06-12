package toluog.campusbash.data

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.utils.AppContract

class TicketsDataSource {
    private val liveTickets = MutableLiveData<List<BoughtTicket>>()
    private val tickets = arrayListOf<BoughtTicket>()
    private val TAG = TicketsDataSource::class.java.simpleName

    fun initListener(mFirestore: FirebaseFirestore, uid: String){
        Log.d(TAG, "initListener")
        val query = mFirestore.collection(AppContract.FIREBASE_EVENTS)
        query.whereEqualTo(AppContract.BUYER_ID, uid)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.d(TAG, "onEvent:error", e)
                    return@EventListener
                }

                // Dispatch the event
                for (change in value.documentChanges) {
                    if(!change.document.exists()) continue
                    // Snapshot of the changed document
                    Log.d(TAG, change.document.toString())
                    val snapshot = change.document.toObject(BoughtTicket::class.java)

                    when (change.type) {
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
                liveTickets.postValue(tickets)
            })
    }

    fun getTickets() = liveTickets

    private fun indexOf(ticket: BoughtTicket): Int {
        for (i in 0 until tickets.size) {
            if(tickets[i].ticketId == ticket.ticketId) return i
        }
        return -1
    }
}