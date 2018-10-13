package toluog.campusbash.data.datasource

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class UniversityDataSource(context: Context, override val coroutineContext: CoroutineContext): DataSource() {

    private var db = AppDatabase.getDbInstance(context)
    private val TAG = UniversityDataSource::class.java.simpleName
    private var listener: ListenerRegistration? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val threadJob = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun listenToUniversities(){
        val query = firestore.collection(AppContract.FIREBASE_UNIVERSITIES)
        Log.d(TAG, "initListener")
        val uniDao = db?.universityDao()
        listener?.remove()
        listener = query.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.d(TAG, "onEvent:error", e)
                return@EventListener
            }

            // Dispatch the event
            this.launch(threadJob) {
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

    override fun clear() {
        listener?.remove()
        threadJob.cancel()
    }

}