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
            db = AppDatabase.getDbInstance(context)
            query.addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot,
                                     e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.w(TAG, "onEvent:error", e)
                        return
                    }

                    // Dispatch the event
                    if (value != null) {
                        for (change in value.getDocumentChanges()) {
                            // Snapshot of the changed document
                            val snapshot = change.getDocument().toObject(Event::class.java)

                            when (change.getType()) {
                                DocumentChange.Type.ADDED -> {
                                    launch { db?.eventDao()?.insertEvent(snapshot) }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    launch { db?.eventDao()?.updateEvent(snapshot) }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    launch { db?.eventDao()?.deleteEvent(snapshot) }
                                }
                            }// TODO: handle document added
                            // TODO: handle document modified
                            // TODO: handle document removed
                        }
                    }

                }
            })
        }
    }
}