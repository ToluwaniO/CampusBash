package toluog.campusbash.data

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import toluog.campusbash.model.Event
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import kotlinx.coroutines.experimental.launch
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import java.util.concurrent.TimeUnit


/**
 * Created by oguns on 12/7/2017.
 */
class EventDataSource  {

    companion object {
        private var db: AppDatabase? = null
        private val TAG = EventDataSource::class.java.simpleName
        private var alarmSet = 0
        private var myEventsPulled = false
        private var lastUniversityPulled = ""
        private var listener: ListenerRegistration? = null

        fun initListener(mFirestore: FirebaseFirestore, context: Context, university: String = ""){
            Log.d(TAG, "initListener")
            alarmSet = Util.getPrefInt(context, AppContract.PREF_FIRST_PLACE_ALARM)
            db = AppDatabase.getDbInstance(context)
            val query = mFirestore.collection(AppContract.FIREBASE_EVENTS)
            if(lastUniversityPulled != university) {
                listener?.remove()
                activateGeneralEventListener(context, query, university)
                lastUniversityPulled = university
            }
            if(!myEventsPulled) {
                activateMyEventListener(context, query)
            }
        }

        private fun activateGeneralEventListener(context: Context, query: CollectionReference, university: String) {
            Log.d(TAG, "activateGeneralEventListener(context= $context, query= $query, university= $university)")
            query.whereGreaterThanOrEqualTo("endTime", System.currentTimeMillis())
                    .whereEqualTo("university", university)
                    .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                        if (e != null) {
                            Log.d(TAG, "onEvent:error", e)
                            return@EventListener
                        }

                        launch {
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
            val uid = FirebaseManager.getUser()?.uid ?: return
            myEventsPulled = true

            listener = query.whereEqualTo(FirestorePaths.EVENT_UID, uid)
                    .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                        if (e != null) {
                            Log.d(TAG, "onEvent:error", e)
                            return@EventListener
                        }

                        launch {
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
            return true
        }

        private fun getAlarmManager(context: Context): AlarmManager {
            return (context.getSystemService(Context.ALARM_SERVICE)) as AlarmManager
        }
    }
}