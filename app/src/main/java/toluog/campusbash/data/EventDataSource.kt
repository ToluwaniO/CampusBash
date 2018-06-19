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
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by oguns on 12/7/2017.
 */
class EventDataSource()  {

    companion object {
        private var db: AppDatabase? = null
        private val TAG = EventDataSource::class.java.simpleName
        private var alarmSet = 0

        fun initListener(mFirestore: FirebaseFirestore, context: Context){
            Log.d(TAG, "initListener")
            alarmSet = Util.getPrefInt(context, AppContract.PREF_FIRST_PLACE_ALARM)
            db = AppDatabase.getDbInstance(context)
            val university = Util.getPrefString(context, AppContract.PREF_UNIVERSITY_KEY)
            val query = mFirestore.collection(AppContract.FIREBASE_EVENTS)
            activateGeneralEventListener(context, query, university)
            activateMyEventListener(context, query)
        }

        private fun activateGeneralEventListener(context: Context, query: CollectionReference, university: String) {
            query.whereGreaterThanOrEqualTo("endTime", System.currentTimeMillis())
                    .whereEqualTo("university", university)
                    .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                        if (e != null) {
                            Log.d(TAG, "onEvent:error", e)
                            return@EventListener
                        }

                        launch {
                            // Dispatch the event
                            for (change in value.documentChanges) {
                                if(!change.document.exists()) continue
                                // Snapshot of the changed document
                                Log.d(TAG, change.document.toString())
                                val snapshot = change.document.toObject(Event::class.java)

                                when (change.type) {
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
                    })
        }

        private fun activateMyEventListener(context: Context, query: CollectionReference) {
            val uid = FirebaseManager.getUser()?.uid ?: return

            query.whereEqualTo(FirestorePaths.EVENT_UID, uid)
                    .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                        if (e != null) {
                            Log.d(TAG, "onEvent:error", e)
                            return@EventListener
                        }

                        launch {
                            // Dispatch the event
                            for (change in value.documentChanges) {
                                if(!change.document.exists()) continue
                                // Snapshot of the changed document
                                Log.d(TAG, change.document.toString())
                                val snapshot = change.document.toObject(Event::class.java)

                                when (change.type) {
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
                    })
        }

        @SuppressLint("RestrictedApi")
        private fun savePlace(id: String, event: Event, context: Context) {
            val place = db?.placeDao()?.getStaticPlace(id)
            Log.d(TAG, "$place")
            val fetchPlace = if(place == null) {
                true
            } else {
                System.currentTimeMillis() - place.timeSaved >= TimeUnit.SECONDS.toMillis(30)
            }

            if(fetchPlace) {
                GeneralDataSource.fetchPlace(id, event, context)
            } else {
                Log.d(TAG, "Place already saved")
            }
        }

        private fun getAlarmManager(context: Context): AlarmManager {
            return (context.getSystemService(Context.ALARM_SERVICE)) as AlarmManager
        }
    }
}