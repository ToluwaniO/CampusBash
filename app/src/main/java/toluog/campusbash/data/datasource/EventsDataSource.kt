package toluog.campusbash.data.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import toluog.campusbash.data.*
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.AppContract
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class EventsDataSource(val context: Context, override val coroutineContext: CoroutineContext): DataSource() {
    private val TAG = EventsDataSource::class.java.simpleName
    private val firestore = FirebaseFirestore.getInstance()
    private val db = AppDatabase.getDbInstance(context)
    private var eventListener: ListenerRegistration? = null
    private var ticketListener: ListenerRegistration? = null
    private val threadJob = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val froshGroup = MutableLiveData<Set<String>>()

    fun listenForEvents(path: String, queries: Set<FirestoreQuery>, university: String? = null) {
        eventListener?.remove()
        val ref = firestore.collection(path)
        val query = FirestoreUtils.buildQuery(ref, queries)
        eventListener = query.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                Log.d(TAG, e.message)
                return@addSnapshotListener
            }
            this.launch(threadJob) {
                querySnapshot?.documentChanges?.forEach {
                    if(it.document.exists()) {
                        val event = it.document.toObject(Event::class.java)
                        if (event.university.isNotBlank() && event.universities.isEmpty()) {
                            event.universities.add(event.university)
                        }
                        event.university = university ?: ""
                        handleEventAction(event, it.type)
                    }
                }
            }
        }
    }

    private suspend fun handleEventAction(event: Event, type: DocumentChange.Type) {
        Log.d(TAG, "$event")

        when (type) {
            DocumentChange.Type.ADDED -> {
                Log.d(TAG, "ChildAdded")
                db?.eventDao()?.insertEvent(event)
            }
            DocumentChange.Type.MODIFIED -> {
                Log.d(TAG, "ChildModified")
                db?.eventDao()?.updateEvent(event)
            }
            DocumentChange.Type.REMOVED -> {
                Log.d(TAG, "ChildRemoved")
                db?.eventDao()?.deleteEvent(event)
            }
        }
        PlaceUtil.savePlace(event.placeId, this@EventsDataSource.context)
    }

    fun getTicketData(eventId: String): LiveData<List<Ticket>> {
        val tickets = MutableLiveData<List<Ticket>>()
        ticketListener?.remove()
        val ref = firestore.collection(AppContract.FIREBASE_EVENTS)
                .document(eventId).collection(AppContract.FIREBASE_EVENT_TICKETS)
        ticketListener = ref.addSnapshotListener { querySnapshot, err ->
            if (err != null) {
                Log.d(TAG, err.message)
                return@addSnapshotListener
            }
            this.launch {
                val tks = arrayListOf<Ticket>()
                for (doc in querySnapshot?.documents ?: emptyList()) {
                    val tk = doc.toObject(Ticket::class.java)
                    if (tk != null) {
                        tks.add(tk)
                    }
                }
                tickets.postValue(tks)
            }
        }
        return tickets
    }

    fun downloadEvent(eventId: String) {
        val ref = firestore.collection("events").document(eventId)
        ref.get().addOnSuccessListener {
            if (!it.exists()) return@addOnSuccessListener
            this.launch(threadJob) {
                val event = it.toObject(Event::class.java)
                if (event != null) {
                    PlaceUtil.savePlace(event.placeId, this@EventsDataSource.context)
                    db?.eventDao()?.insertEvent(event)
                }
            }
        }.addOnFailureListener {
            Log.d(TAG, it.message)
        }
    }

    fun listenToFroshGroup(): LiveData<Set<String>> {
        val ref = firestore.collection("eventGroup").document("ess")
        ref.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.e(TAG, e.message)
                return@addSnapshotListener
            }
            this.launch(threadJob) {
                Log.d(TAG, documentSnapshot.toString())
                val set = (documentSnapshot?.get("idList") as List<String>? ?: emptyList()).toHashSet()
                froshGroup.postValue(set)
            }
        }
        return froshGroup
    }

    private fun constructQuery(ref: CollectionReference, queries: Set<FirestoreQuery>) {
        queries.forEach {
            addQuery(ref, it)
        }
    }

    private fun addQuery(ref: CollectionReference, query: FirestoreQuery) {
        when (query.queryType) {
            FirestoreQueryType.EQUAL_TO -> ref.whereEqualTo(query.key, query.value)
            FirestoreQueryType.ARRAY_CONTAINS -> ref.whereArrayContains(query.key, query.value ?: Any())
            FirestoreQueryType.GREATER_THAN -> ref.whereGreaterThan(query.key, query.value ?: Any())
            FirestoreQueryType.LESS_THAN -> ref.whereLessThan(query.key, query.value ?: Any())
            FirestoreQueryType.GREATER_THAN_EQUAL_TO -> ref.whereGreaterThanOrEqualTo(query.key, query.value ?: Any())
            FirestoreQueryType.LESS_THAN_EQUAL_TO -> ref.whereLessThanOrEqualTo(query.key, query.value ?: Any())
        }
    }

    private fun getUniversity(queries: Set<FirestoreQuery>): String? {
        for (query in queries) {
            if (query.key == "university") return query.value as String?
        }
        return null
    }

    override fun clear() {
        eventListener?.remove()
        ticketListener?.remove()
        eventListener = null
        threadJob.cancel()
    }

}