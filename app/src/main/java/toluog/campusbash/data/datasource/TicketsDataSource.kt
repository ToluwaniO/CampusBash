package toluog.campusbash.data.datasource

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.utils.AppContract
import java.util.concurrent.Executors

class TicketsDataSource: DataSource {

    private val liveTickets = MutableLiveData<List<BoughtTicket>>()
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
                    value?.documents?.forEach {
                        if (it.exists() && validate(it)) {
                            Log.d(TAG, it.toString())
                            val snapshot = it.toObject(BoughtTicket::class.java)
                            if (snapshot != null) {
                                tickets.add(snapshot)
                            }
                        }
                    }
                    liveTickets.postValue(tickets)
                }
            })
    }

    fun getTickets() = liveTickets

    private fun validate(doc: DocumentSnapshot): Boolean {
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