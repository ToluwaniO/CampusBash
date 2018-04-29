package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.android.gms.ads.formats.NativeAd
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import toluog.campusbash.data.Repository
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.utils.AdManager

/**
 * Created by oguns on 12/13/2017.
 */
class EventsViewModel(app: Application) : AndroidViewModel(app) {

    private var events: LiveData<List<Event>>? = null
    private var places: LiveData<List<Place>>? = null
    private val ads: ArrayList<NativeAd> = ArrayList()
    private val adsList = MutableLiveData<ArrayList<NativeAd>>()
    private val repo: Repository = Repository(app.applicationContext, FirebaseFirestore.getInstance())
    private val TAG = EventsViewModel::class.java.simpleName
    private  val adManager: AdManager
    private var adsJob: Job? = null

    init {
        AdManager.initializeAds(app.applicationContext)
        adsList.value = ads
        adManager = AdManager(app.applicationContext, adsList)
        init()
    }

    fun init() {
        places = repo.getPlaces()
        events = repo.getEvents()
    }

    fun loadAds() {
        Log.d(TAG, "loading ads...")
        adsJob = launch { adManager.loadAds() }
    }

    fun getEvents(): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents called")
        return events
    }

    fun getEvents(name: String, type: String, time: Long): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents called")
        return repo.getEventsWithQueryAndType(name, type, time)
    }

    fun getEvents(name: String, time: Long): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents called")
        return repo.getEventsWithQuery(name, time)
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
        adsJob?.cancel()
        super.onCleared()
    }
}