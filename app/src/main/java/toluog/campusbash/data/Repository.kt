package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.doAsync
import toluog.campusbash.data.network.ServerResponse
import toluog.campusbash.data.network.StripeAccountBody
import toluog.campusbash.data.network.StripeServerClient
import toluog.campusbash.model.Currency
import toluog.campusbash.utils.AppContract
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.model.University
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

/**
 * Created by oguns on 12/7/2017.
 */
class Repository(c: Context, mFirebaseFirestore: FirebaseFirestore){
    private val TAG = Repository::class.java.simpleName
    private val eventDataSource = EventDataSource()
    private val db: AppDatabase? = AppDatabase.getDbInstance(c)
    private val mFireStore = FirebaseFirestore.getInstance()
    private lateinit var stripeApi: StripeServerClient
    private val context = c
    private var initializedEvents = false
    private var initializedUnis = false
    private var initializedStripeApi = false


    fun getEvents() : LiveData<List<Event>>?{
        Log.d(TAG, "getEvents called")
        if(!initializedEvents){
            EventDataSource.initListener(FirebaseFirestore.getInstance(), context)
            initializedEvents = true
            Log.d(TAG, "datasource initialized")
        }
        return db?.eventDao()?.getEvents(System.currentTimeMillis())
    }

    fun getEvent(eventId: String): LiveData<Event>?{
        return db?.eventDao()?.getEvent(eventId)
    }

    fun getEventsWithQueryAndType(name: String, type: String, time: Long): LiveData<List<Event>>?{
        return db?.eventDao()?.getEventsWithQueryAndType(name, type, time)
    }

    fun getEventsWithQuery(name: String, time: Long): LiveData<List<Event>>?{
        return db?.eventDao()?.getEventsWithQuery(name, time)
    }

    fun addEvent(event: Event){
        val collectionRef = mFireStore.collection(AppContract.FIREBASE_EVENTS)
        collectionRef.add(event)
    }

    fun getUnis(country: String): LiveData<List<University>>? {
        Log.d(TAG, "get unis called")
        if(!initializedUnis){
            UniversityDataSource.initListener(context)
            initializedUnis = true
        }
        return db?.universityDao()?.getUniversities(country)
    }

    fun getCurrencies(): LiveData<List<Currency>>? {
        Util.downloadCurrencies(context)
        return db?.currencyDao()?.getCurrencies()
    }

    fun getUser(uid: String) = GeneralDataSource.getUser(mFireStore, uid)

    fun getPlaces() = db?.placeDao()?.getPlaces()

    fun getPlace(id: String) = db?.placeDao()?.getPlace(id)

    fun createStripeAccount(): LiveData<ServerResponse> {
        val user = FirebaseManager.getUser()
        if(!initializedStripeApi && user != null) {
            stripeApi = StripeServerClient()
            initializedStripeApi = true
        }
        val body = StripeAccountBody(user?.uid ?: "", user?.email ?: "", "CA")
        return stripeApi.createStripeAccount(body)
    }

    fun savePlace(place: Place) {
        doAsync { db?.placeDao()?.insertPlace(place) }
    }

    fun deleteOldEvents() {
        db?.eventDao()?.deleteEvents(System.currentTimeMillis())
    }

}