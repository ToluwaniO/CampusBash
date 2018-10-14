package toluog.campusbash.data.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import org.jetbrains.anko.doAsync
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.datasource.*
import toluog.campusbash.data.network.ServerResponse
import toluog.campusbash.data.network.StripeAccountBody
import toluog.campusbash.data.network.StripeServerClient
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.model.Place
import toluog.campusbash.model.PublicProfile
import toluog.campusbash.model.University
import toluog.campusbash.utils.FirebaseManager
import kotlin.coroutines.CoroutineContext

class GeneralRepository(val context: Context, override val coroutineContext: CoroutineContext): Repository() {
    private val TAG = GeneralRepository::class.java.simpleName
    private val db = AppDatabase.getDbInstance(context)
    private val ticketsDataSource = TicketsDataSource(coroutineContext)
    private val publicProfileDataSource = PublicProfileDataSource(coroutineContext)
    private val profileEventsDataSource = EventsDataSource(context, coroutineContext)

    fun listenForUniversities() {
        UniversityDataSource(context, coroutineContext).listenToUniversities()
    }

    fun getUniversities(country: String) = db?.universityDao()?.getUniversities(country)

    fun getUniversities() = db?.universityDao()?.getUniversities()

    fun getCurrencies() = db?.currencyDao()?.getCurrencies()

    fun getPlace(id: String) = db?.placeDao()?.getPlace(id)

    fun getPlaces() = db?.placeDao()?.getPlaces()

    fun savePlace(place: Place) {
        doAsync { db?.placeDao()?.insertPlace(place) }
    }

    fun getUser(uid: String) = GeneralDataSource.getUser(uid)

    fun deleteOldEvents() {
        val uid = FirebaseManager.getUser()?.uid ?: ""
        db?.eventDao()?.deleteOldEvents(System.currentTimeMillis(), uid)
    }

    fun getUnis(country: String): LiveData<List<University>>? {
        Log.d(TAG, "get unis called")
        UniversityDataSource(context, coroutineContext).listenToUniversities()
        return db?.universityDao()?.getUniversities(country)
    }

    fun getFroshGroup() = EventsDataSource(context, coroutineContext).listenToFroshGroup()

    suspend fun createStripeAccount(): ServerResponse {
        val user = FirebaseManager.getUser()
        val stripeApi = StripeServerClient()
        val body = StripeAccountBody(user?.uid ?: "", user?.email ?: "", "CA")
        return stripeApi.createStripeAccount(body, user?.uid ?: "")
    }

    fun getTickets(): LiveData<List<BoughtTicket>> {
        ticketsDataSource.initListener(FirebaseManager.getUser()?.uid ?: "")
        return ticketsDataSource.getTickets()
    }

    fun getPublicProfile(uid: String): LiveData<PublicProfile?> {
        publicProfileDataSource.initListener(uid)
        return publicProfileDataSource.liveProfile
    }

    fun getFollowers(uid: String): LiveData<List<PublicProfile>> {
        publicProfileDataSource.initListener(uid)
        return publicProfileDataSource.liveFollowers
    }

    fun getFollowing(uid: String): LiveData<List<PublicProfile>> {
        publicProfileDataSource.initListener(uid)
        return publicProfileDataSource.liveFollowing
    }

    fun getUserEvents(uid: String) = profileEventsDataSource.getUserEvents(uid)

    fun destroyUserEventListener() {
        profileEventsDataSource.clear()
    }

    override fun clear() {
        ticketsDataSource.clear()
        publicProfileDataSource.clear()
        profileEventsDataSource.clear()
    }

}