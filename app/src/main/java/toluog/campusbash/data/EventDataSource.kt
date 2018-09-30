package toluog.campusbash.data

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import toluog.campusbash.model.Event
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.doAsync
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * Created by oguns on 12/7/2017.
 */
object EventDataSource  {

    private var db: AppDatabase? = null
    private val TAG = EventDataSource::class.java.simpleName
    private var lastUniversityPulled = ""
    private var lastUid = ""
    private var lastUserEventsUid = ""
    private val froshGroup = MutableLiveData<Set<String>>()
    private var listener: ListenerRegistration? = null
    private var myEventsListener: ListenerRegistration? = null
    private var froshGroupListener: ListenerRegistration? = null
    private var userEventsListener: ListenerRegistration? = null
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val myEventsDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private var userEvents = MutableLiveData<ArrayList<Event>?>()

    fun initListener(mFirestore: FirebaseFirestore, context: Context, university: String = ""){
        Log.d(TAG, "initListener")
        val uid = FirebaseManager.getUser()?.uid
        db = AppDatabase.getDbInstance(context)
        val query = mFirestore.collection(AppContract.FIREBASE_EVENTS)
        if(lastUniversityPulled != university && !university.isBlank()) {
            listener?.remove()
            activateGeneralEventListener(context, query, university)
            lastUniversityPulled = university
        }
        if(lastUid != uid) {
            myEventsListener?.remove()
            activateMyEventListener(context, query)
        }
        listenToFroshGroup(mFirestore)
    }

    fun downloadEvent(eventId: String, mFirestore: FirebaseFirestore, context: Context) {
        db = AppDatabase.getDbInstance(context)
        val ref = mFirestore.collection(AppContract.FIREBASE_EVENTS).document(eventId)
        ref.get().addOnSuccessListener {
            if (!it.exists()) {
                Log.e(TAG, "Event does not exist")
                return@addOnSuccessListener
            }
            val event = it.toObject(Event::class.java)
            if (event != null) {
                launch(dispatcher) {
                    db?.eventDao()?.insertEvent(event)
                    savePlace(event.placeId, event, context)
                }
            }
        }.addOnFailureListener {
            Log.e(TAG, it.message)
            Log.e(TAG, it.toString())
        }
    }

    fun listenToFroshGroup(mFirestore: FirebaseFirestore) {
        if (froshGroupListener != null) return
        val ref = mFirestore.collection("eventGroup").document("ess")
        froshGroupListener = ref.addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Log.e(TAG, e.message)
                        return@addSnapshotListener
                    }
                    Log.d(TAG, documentSnapshot.toString())
                    val set = (documentSnapshot?.get("idList") as List<String>? ?: emptyList()).toHashSet()
                    froshGroup.postValue(set)
                }
    }

    fun getUserEvents(uid: String): MutableLiveData<ArrayList<Event>?> {
        if (uid == lastUserEventsUid) return userEvents
        lastUserEventsUid = uid
        userEventsListener?.remove()
        fetchUserEvents(uid)
        return userEvents
    }

    fun destroyUserEventListener() {
        lastUserEventsUid = ""
        userEventsListener?.remove()
        userEvents = MutableLiveData<ArrayList<Event>?>()
    }

    private fun activateGeneralEventListener(context: Context, query: CollectionReference, university: String) {
        Log.d(TAG, "activateGeneralEventListener(context= $context, query= $query, university= $university)")
        listener = query.whereGreaterThanOrEqualTo("endTime", System.currentTimeMillis())
                .whereEqualTo("university", university)
                .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                    if (e != null) {
                        Log.d(TAG, "onEvent:error", e)
                        return@EventListener
                    }

                    launch(dispatcher) {
                        // Dispatch the event
                        value?.documentChanges?.forEach {
                            if(it.document.exists() && validate(it)) {
                                // Snapshot of the changed document
                                Log.d(TAG, it.document.toString())
                                val snapshot = it.document.toObject(Event::class.java)
                                Log.d(TAG, snapshot.toString())

                                when (it.type) {
                                    DocumentChange.Type.ADDED -> {
                                        Log.d(TAG, "ChildAdded")
                                        db?.eventDao()?.insertEvent(snapshot)
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        Log.d(TAG, "ChildModified")
                                        db?.eventDao()?.updateEvent(snapshot)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        Log.d(TAG, "ChildRemoved")
                                        db?.eventDao()?.deleteEvent(snapshot)
                                    }
                                }
                                savePlace(snapshot.placeId, snapshot, context)
                            }
                        }
                    }
                })
    }

    private fun activateMyEventListener(context: Context, query: CollectionReference) {
        Log.d(TAG, "activateGeneralEventListener(context= $context, query= $query)")
        lastUid = FirebaseManager.getUser()?.uid ?: return
        myEventsListener = query.whereEqualTo(FirestorePaths.EVENT_UID, lastUid)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.d(TAG, "onEvent:error", e)
                    return@EventListener
                }

                launch(myEventsDispatcher) {
                    // Dispatch the event
                    value?.documentChanges?.forEach {
                        if(!it.document.exists()) {
                            // Snapshot of the changed document
                            Log.d(TAG, it.document.toString())
                            val snapshot = it.document.toObject(Event::class.java)
                            Log.d(TAG, snapshot.toString())

                            when (it.type) {
                                DocumentChange.Type.ADDED -> {
                                    Log.d(TAG, "ChildAdded")
                                    db?.eventDao()?.insertEvent(snapshot)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    Log.d(TAG, "ChildModified")
                                    db?.eventDao()?.updateEvent(snapshot)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    Log.d(TAG, "ChildRemoved")
                                    db?.eventDao()?.deleteEvent(snapshot)
                                }
                            }
                            savePlace(snapshot.placeId, snapshot, context)
                        }
                    }
                }
            })
    }

    private fun fetchUserEvents(uid: String) {
        val query = FirebaseFirestore.getInstance().collection("events").whereEqualTo("creator.uid", uid)
        userEventsListener = query.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                Log.d(TAG, e.message)
            }

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

    @SuppressLint("RestrictedApi")
    private fun savePlace(id: String, event: Event, context: Context) {
        val place = db?.placeDao()?.getStaticPlace(id)
        Log.d(TAG, "$place")
        val fetchPlace = if(place == null) {
            true
        } else {
            System.currentTimeMillis() - place.timeSaved >= TimeUnit.DAYS.toMillis(30)
        }

        if(fetchPlace) {
            GeneralDataSource.fetchPlace(id, event, context)
        } else {
            Log.d(TAG, "Place already saved")
        }
    }

    private fun validate(snap: DocumentChange): Boolean {
        val doc = snap.document
        if(doc["eventId"] == null) return false
        if(doc["eventName"] == null) return false
        if(doc["eventType"] == null) return false
        if(doc["university"] == null) return false
        if(doc["startTime"] == null) return false
        if(doc["endTime"] == null) return false
        if(doc["placeId"] == null) return false
        if(doc["tickets"] == null) return false
        if(doc["creator"] == null) return false
        if(doc["ticketsSold"] == null) return false
        if(doc["timeZone"] == null) return false
        return true
    }

    fun getFroshGroup() = froshGroup
}