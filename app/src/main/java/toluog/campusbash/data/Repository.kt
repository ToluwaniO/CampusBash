package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.utils.AppContract
import toluog.campusbash.model.Event

/**
 * Created by oguns on 12/7/2017.
 */
class Repository(c: Context){
    private val TAG = Repository::class.java.simpleName
    private val eventDataSource = EventDataSource()
    private val db: AppDatabase? = AppDatabase.getDbInstance(c)
    private val mFireStore = FirebaseFirestore.getInstance()
    private val context = c
    private var initialized = false
    fun getEvents() : LiveData<List<Event>>?{
        Log.d(TAG, "getEvents called")
        if(initialized == false){
            EventDataSource.initListener(context)
            initialized = true
            Log.d(TAG, "datasource initialized")
        }
        return db?.eventDao()?.getEvents()
    }

    fun getEvent(eventId: String): LiveData<Event>?{
        return db?.eventDao()?.getEvent(eventId)
    }

    fun addEvent(event: Event){
        val collectionRef = mFireStore.collection(AppContract.FIREBASE_EVENTS)
        collectionRef.add(event)
    }

}