package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.doAsync
import toluog.campusbash.data.network.ServerResponse
import toluog.campusbash.data.network.StripeAccountBody
import toluog.campusbash.data.network.StripeServerClient
import toluog.campusbash.model.*
import toluog.campusbash.model.dashboard.UserTicket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

/**
 * Created by oguns on 12/7/2017.
 */
class Repository(val context: Context){
    
    private val TAG = Repository::class.java.simpleName
    private val db: AppDatabase? = AppDatabase.getDbInstance(context)
    private val mFireStore = FirebaseFirestore.getInstance()
    private lateinit var stripeApi: StripeServerClient
    private var initializedStripeApi = false

    fun downloadEvent(eventId: String, context: Context) {
        EventDataSource.downloadEvent(eventId, FirebaseFirestore.getInstance(), context)
    }
    
    fun getEvents(university: String): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents(university= $university) called")
        EventDataSource.initListener(FirebaseFirestore.getInstance(), context, university)
        Log.d(TAG, "datasource initialized")
        return db?.eventDao()?.getEvents(university, System.currentTimeMillis())
    }

    fun getMyEvents() : LiveData<List<Event>>? {
        Log.d(TAG, "getEvents called")
        EventDataSource.initListener(FirebaseFirestore.getInstance(), context)
        Log.d(TAG, "datasource initialized")
        return db?.eventDao()?.getMyEvents(FirebaseManager.getUser()?.uid ?: "")
    }

    fun getEvent(eventId: String): LiveData<Event>? {
        return db?.eventDao()?.getEvent(eventId)
    }

    fun getEventsWithQueryAndType(name: String, type: String, time: Long): LiveData<List<Event>>? {
        return db?.eventDao()?.getEventsWithQueryAndType(name, type, time)
    }

    fun getEventsWithQuery(name: String, time: Long): LiveData<List<Event>>? {
        return db?.eventDao()?.getEventsWithQuery(name, time)
    }

    fun getUnis(country: String): LiveData<List<University>>? {
        Log.d(TAG, "get unis called")
        UniversityDataSource.initListener(context)
        return db?.universityDao()?.getUniversities(country)
    }

    fun getCurrencies(): LiveData<List<Currency>>? {
        Util.downloadCurrencies(context)
        return db?.currencyDao()?.getCurrencies()
    }

    fun getTickets(): LiveData<List<BoughtTicket>> {
        TicketsDataSource.initListener(FirebaseFirestore.getInstance(),
                FirebaseManager.getUser()?.uid ?: "")
        return TicketsDataSource.getTickets()
    }

    fun getUser(uid: String) = GeneralDataSource.getUser(mFireStore, uid)

    fun getPlaces() = db?.placeDao()?.getPlaces()

    fun getPlace(id: String) = db?.placeDao()?.getPlace(id)

    fun getFroshGroup() : LiveData<Set<String>> {
        EventDataSource.listenToFroshGroup(FirebaseFirestore.getInstance())
        return EventDataSource.getFroshGroup()
    }

    fun createStripeAccount(): LiveData<ServerResponse> {
        val user = FirebaseManager.getUser()
        if(!initializedStripeApi && user != null) {
            stripeApi = StripeServerClient()
            initializedStripeApi = true
        }
        val body = StripeAccountBody(user?.uid ?: "", user?.email ?: "", "CA")
        return stripeApi.createStripeAccount(body, user?.uid ?: "")
    }

    fun savePlace(place: Place) {
        doAsync { db?.placeDao()?.insertPlace(place) }
    }

    fun deleteOldEvents() {
        db?.eventDao()?.deleteEvents(System.currentTimeMillis())
    }

    fun getUserWithTickets(eventId: String): LiveData<List<UserTicket>> {
        EventDashboardDataSource.initListener(FirebaseFirestore.getInstance(), eventId)
        return EventDashboardDataSource.getTickets()
    }

    fun getTicketMetaDatas(eventId: String): LiveData<Map<String, TicketMetaData>> {
        EventDashboardDataSource.initListener(FirebaseFirestore.getInstance(), eventId)
        return EventDashboardDataSource.getMetadatas()
    }

    fun getPublicProfile(uid: String): LiveData<PublicProfile?> {
        PublicProfileDataSource.initListener(uid)
        return PublicProfileDataSource.liveProfile
    }

    fun getFollowers(uid: String): LiveData<List<PublicProfile>> {
        PublicProfileDataSource.initListener(uid)
        return PublicProfileDataSource.liveFollowers
    }

    fun getFollowing(uid: String): LiveData<List<PublicProfile>> {
        PublicProfileDataSource.initListener(uid)
        return PublicProfileDataSource.liveFollowing
    }

    fun getUserEvents(uid: String) = EventDataSource.getUserEvents(uid)
}