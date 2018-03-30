package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.data.network.StripeAccountBody
import toluog.campusbash.data.network.StripeServerClient
import toluog.campusbash.model.Currency
import toluog.campusbash.utils.AppContract
import toluog.campusbash.model.Event
import toluog.campusbash.model.University
import toluog.campusbash.utils.FirebaseManager

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
        if(initializedEvents == false){
            EventDataSource.initListener(context)
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
        if(initializedUnis == false){
            UniversityDataSource.initListener(context)
            initializedUnis = true
        }
        return db?.universityDao()?.getUniversities(country)
    }

    fun getCurrencies() = db?.currencyDao()?.getCurrencies()

    fun getUser(uid: String) = GeneralDataSource.getUser(mFireStore, uid)

    fun createStripeAccount(): LiveData<HashMap<String, Any>>? {
        val user = FirebaseManager.getUser()
        if(!initializedStripeApi && user != null) {
            val body = StripeAccountBody(user.uid, user.email ?: "", "CA")
            stripeApi = StripeServerClient(body)
            initializedStripeApi = true
        }
        return stripeApi.createStripeAccount()
    }


}