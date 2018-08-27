package toluog.campusbash.data

import android.annotation.SuppressLint
import android.app.AlarmManager
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
    private var listener: ListenerRegistration? = null
    private var myEventsListener: ListenerRegistration? = null
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val myEventsDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

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
                }
            }
        }.addOnFailureListener {
            Log.e(TAG, it.message)
            Log.e(TAG, it.toString())
        }
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
        if (doc["timeZone"] == null) return false
        return true
    }
}