package toluog.campusbash.data.repository

import androidx.lifecycle.LiveData
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.datasource.GeneralDataSource
import toluog.campusbash.data.datasource.EventsDataSource
import toluog.campusbash.data.datasource.TicketsDataSource
import toluog.campusbash.data.datasource.UniversityDataSource
import toluog.campusbash.data.network.ServerResponse
import toluog.campusbash.data.network.StripeAccountBody
import toluog.campusbash.data.network.StripeServerClient
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.model.Place
import toluog.campusbash.model.University
import toluog.campusbash.utils.FirebaseManager
import kotlin.coroutines.CoroutineContext

class GeneralRepository(val context: Context, override val coroutineContext: CoroutineContext): Repository() {
    private val TAG = GeneralRepository::class.java.simpleName
    private val db = AppDatabase.getDbInstance(context)
    private val ticketsDataSource = TicketsDataSource(coroutineContext)

    private fun listenForUniversities() {
        UniversityDataSource(context, coroutineContext).listenToUniversities()
    }

    fun getUniversities(country: String) = db?.universityDao()?.getUniversities(country)

    fun getUniversities() = db?.universityDao()?.getUniversities()

    fun getCurrencies() = db?.currencyDao()?.getCurrencies()

    fun getPlace(id: String) = db?.placeDao()?.getPlace(id)

    fun getPlaces() = db?.placeDao()?.getPlaces()

    fun savePlace(place: Place) {
        this.launch { db?.placeDao()?.insertPlace(place) }
    }

    fun getUser(uid: String) = GeneralDataSource.getUser(uid)

    fun deleteOldEvents() {
        val uid = FirebaseManager.getUser()?.uid ?: ""
        db?.eventDao()?.deleteOldEvents(System.currentTimeMillis(), uid)
    }

    fun getUnis(country: String): LiveData<List<University>>? {
        Log.d(TAG, "get unis called")
        listenForUniversities()
        return db?.universityDao()?.getUniversities(country)
    }

    fun getFroshGroup() = EventsDataSource(context, coroutineContext).listenToFroshGroup()

    suspend fun createStripeAccount(): ServerResponse {
        val user = FirebaseManager.getUser()
        val stripeApi = StripeServerClient()
        val body = StripeAccountBody(user?.uid ?: "", user?.email ?: "", "CA")
        return stripeApi.createStripeAccount(body)
    }

    fun getTickets(): LiveData<List<BoughtTicket>> {
        ticketsDataSource.initListener(FirebaseManager.getUser()?.uid ?: "")
        return ticketsDataSource.getTickets()
    }

    override fun clear() {
        ticketsDataSource.clear()
    }

}