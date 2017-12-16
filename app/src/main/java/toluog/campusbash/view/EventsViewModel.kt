package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import toluog.campusbash.data.Repository
import toluog.campusbash.model.Event

/**
 * Created by oguns on 12/13/2017.
 */
class EventsViewModel(app: Application) : AndroidViewModel(app) {

    private val events: MutableLiveData<ArrayList<Event>>? = null
    private val repo: Repository = Repository(app.applicationContext)
    private val TAG = EventsViewModel::class.java.simpleName

    fun getEvents(): LiveData<List<Event>>? {
        Log.d(TAG, "getEvents called")
        return repo.getEvents()
    }

}