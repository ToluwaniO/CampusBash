package toluog.campusbash.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import toluog.campusbash.model.Event
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.experimental.launch
import toluog.campusbash.data.UniversityDataSource.Companion.query
import toluog.campusbash.utils.AppContract


/**
 * Created by oguns on 12/7/2017.
 */
class EventDataSource()  {

    companion object {
        var db: AppDatabase? = null
        val TAG = EventDataSource::class.java.simpleName

        fun initListener(mFirestore: FirebaseFirestore, context: Context){
            Log.d(TAG, "initListener")
            db = AppDatabase.getDbInstance(context)
            val query = mFirestore.collection(AppContract.FIREBASE_EVENTS)
            query.whereGreaterThanOrEqualTo("endTime", System.currentTimeMillis()).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
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
                    }
                }
            })
        }
    }
}