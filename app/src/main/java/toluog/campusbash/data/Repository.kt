package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.utils.AppContract
import toluog.campusbash.model.Event
import toluog.campusbash.model.University

/**
 * Created by oguns on 12/7/2017.
 */
class Repository(c: Context){
    private val TAG = Repository::class.java.simpleName
    private val eventDataSource = EventDataSource()
    private val db: AppDatabase? = AppDatabase.getDbInstance(c)
    private val mFireStore = FirebaseFirestore.getInstance()
    private val context = c
    private var initializedEvents = false
    private var initializedUnis = false
    fun getEvents() : LiveData<List<Event>>?{
        Log.d(TAG, "getEvents called")
        if(initializedEvents == false){
            EventDataSource.initListener(context)
            initializedEvents = true
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

    fun getUnis(country: String): LiveData<List<University>>? {
        Log.d(TAG, "get unis called")
        if(initializedUnis == false){
            UniversityDataSource.initListener(context)
            initializedUnis = true
        }
        return db?.universityDao()?.getUniversities(country)
    }

}