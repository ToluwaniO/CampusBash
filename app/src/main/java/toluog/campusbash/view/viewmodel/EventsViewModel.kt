package toluog.campusbash.view.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.android.gms.ads.formats.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import toluog.campusbash.data.repository.EventsRepository
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.utils.AdManager

/**
 * Created by oguns on 12/13/2017.
 */
class EventsViewModel(app: Application) : GeneralViewModel(app) {

    private var places: LiveData<List<Place>>? = null
    private val ads: ArrayList<NativeAd> = ArrayList()
    private val adsList = MutableLiveData<ArrayList<NativeAd>>()
    private val TAG = EventsViewModel::class.java.simpleName
    private val repository = EventsRepository(app.applicationContext)
    private val adManager: AdManager
    private var adsJob: Job = Job()
    private val threadScope = CoroutineScope(adsJob)

    init {
        AdManager.initializeAds(app.applicationContext)
        adsList.value = ads
        adManager = AdManager(app.applicationContext, adsList)
        places = generalRepository.getPlaces()
    }

    private fun loadAds() {
        Log.d(TAG, "loading ads...")
        threadScope.launch { adManager.loadAds() }
    }

    fun getEvents(university: String, myEvents: Boolean = false): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents called")
        if(myEvents) {
            return getMyEvents()
        }
        return getEvents(university)
    }

    private fun getEvents(university: String): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents(university= $university) called")
        return repository.getEvents(university)
    }

    private fun getMyEvents(): LiveData<List<Event>>? {
        Log.d(TAG, "getMyEvents called")
        return repository.getMyEvents()
    }

    fun getEvents(name: String, type: String, time: Long): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents called")
        return repository.getEventsWithQueryAndType(name, type, time)
    }

    fun getEvents(name: String, time: Long): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents called")
        return repository.getEventsWithQuery(name, time)
    }

    fun getPlaces(): LiveData<List<Place>>? {
        return places
    }

    fun getAds(): MutableLiveData<ArrayList<NativeAd>> {
        if(ads.isEmpty()) {
            loadAds()
        }
        Log.d(TAG, "returning ads with size ${ads.size}...")
        return adsList
    }

    override fun onCleared() {
        adsJob.cancel()
        repository.clear()
        super.onCleared()
    }
}