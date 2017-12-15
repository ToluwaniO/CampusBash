package toluog.campusbash.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import io.reactivex.Observable
import toluog.campusbash.model.Event
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import toluog.campusbash.Utils.AppContract


/**
 * Created by oguns on 12/7/2017.
 */
class EventDataSource()  {

    companion object {
        val mFireStore = FirebaseFirestore.getInstance()
        val query = mFireStore.collection(AppContract.FIREBASE_EVENTS)
        var db: AppDatabase? = null
        val TAG = EventDataSource::class.java.simpleName

        fun initListener(context: Context){
            Log.d(TAG, "initListener")
            db = AppDatabase.getDbInstance(context)
            query.addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot,
                                     e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.d(TAG, "onEvent:error", e)
                        return
                    }

                    // Dispatch the event
                    for (change in value.getDocumentChanges()) {
                        // Snapshot of the changed document
                        Log.d(TAG, change.document.toString())
                        val snapshot = change.document.toObject(Event::class.java)

                        when (change.getType()) {
                            DocumentChange.Type.ADDED -> {
                                Log.d(TAG, "ChildAdded")
                                launch { db?.eventDao()?.insertEvent(snapshot) }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Log.d(TAG, "ChildModified")
                                launch { db?.eventDao()?.updateEvent(snapshot) }
                            }
                            DocumentChange.Type.REMOVED -> {
                                Log.d(TAG, "ChildRemoved")
                                launch { db?.eventDao()?.deleteEvent(snapshot) }
                            }
                        }
                    }

                }
            })
        }
    }
}