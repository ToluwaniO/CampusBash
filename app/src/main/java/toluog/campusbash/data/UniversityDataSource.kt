package toluog.campusbash.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.coroutines.experimental.launch
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 12/28/2017.
 */
object UniversityDataSource {

    private var db: AppDatabase? = null
    private val TAG = UniversityDataSource::class.java.simpleName
    private var listener: ListenerRegistration? = null

    fun initListener(context: Context){
        val mFireStore = FirebaseFirestore.getInstance()
        val query = mFireStore.collection(AppContract.FIREBASE_UNIVERSITIES)
        Log.d(TAG, "initListener")
        db = AppDatabase.getDbInstance(context)
        val uniDao = db?.universityDao()
        listener?.remove()
        listener = query.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.d(TAG, "onEvent:error", e)
                return@EventListener
            }

            // Dispatch the event
            value?.documentChanges?.forEach {
                if(it.document.exists() && validate(it)) {
                    // Snapshot of the changed document
                    Log.d(TAG, it.document.toString())
                    val snapshot = it.document.toObject(University::class.java)

                    when (it.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(TAG, "ChildAdded")
                            launch { uniDao?.insertUniversity(snapshot) }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d(TAG, "ChildModified")
                            launch { uniDao?.updateUniversity(snapshot) }
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(TAG, "ChildRemoved")
                            launch { uniDao?.deleteUniversity(snapshot) }
                        }
                    }
                }
            }
        })
    }

    private fun validate(snap: DocumentChange): Boolean {
        val doc = snap.document
        if(doc["uniId"] == null) return false
        if(doc["name"] == null) return false
        if(doc["city"] == null) return false
        if(doc["province"] == null) return false
        if(doc["country"] == null) return false
        if(doc["nickName"] == null) return false
        if(doc["shortName"] == null) return false
        return true
    }
}