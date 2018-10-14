package toluog.campusbash.data.datasource

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import toluog.campusbash.data.*
import toluog.campusbash.model.Event
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class EventsDataSource(val context: Context, override val coroutineContext: CoroutineContext): DataSource() {
    private val TAG = EventsDataSource::class.java.simpleName
    private val firestore = FirebaseFirestore.getInstance()
    private val db = AppDatabase.getDbInstance(context)
    private var eventListener: ListenerRegistration? = null
    private var userEventsListener: ListenerRegistration? = null
    private val threadJob = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val froshGroup = MutableLiveData<Set<String>>()

    fun listenForEvents(path: String, queries: Set<FirestoreQuery>) {
        eventListener?.remove()
        val ref = firestore.collection(path)
        constructQuery(ref, queries)
        eventListener = ref.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                Log.d(TAG, e.message)
                return@addSnapshotListener
            }
            this.launch(threadJob) {
                querySnapshot?.documentChanges?.forEach {
                    if(it.document.exists()) {
                        val event = it.document.toObject(Event::class.java)
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

    fun downloadEvent(path: String) {
        val ref = firestore.document(path)
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

    fun getUserEvents(uid: String): MutableLiveData<ArrayList<Event>?> {
        userEventsListener?.remove()
        return fetchUserEvents(uid)
    }

    private fun fetchUserEvents(uid: String): MutableLiveData<ArrayList<Event>?> {
        val userEvents = MutableLiveData<ArrayList<Event>?>()
        val query = FirebaseFirestore.getInstance().collection("events").whereEqualTo("creator.uid", uid)
        userEventsListener = query.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                Log.d(TAG, e.message)
                return@addSnapshotListener
            }
            this.launch(threadJob) {
                val uEvents = querySnapshot?.documents?.asSequence()?.map {
                    it.toObject(Event::class.java)
                }?.mapNotNull { it }
                userEvents.postValue(ArrayList<Event>().apply {
                    if (uEvents != null) {
                        addAll(uEvents)
                    }
                })
            }

        }
        return userEvents
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

    override fun clear() {
        eventListener?.remove()
        userEventsListener?.remove()
        eventListener = null
        userEventsListener = null
        threadJob.cancel()
    }

}