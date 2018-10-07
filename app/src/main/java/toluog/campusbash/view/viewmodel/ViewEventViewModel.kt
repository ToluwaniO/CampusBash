package toluog.campusbash.view.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import toluog.campusbash.data.repository.EventsRepository
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.view.viewmodel.GeneralViewModel

/**
 * Created by oguns on 12/23/2017.
 */
class ViewEventViewModel(app: Application): GeneralViewModel(app){

    private val repository = EventsRepository(app.applicationContext)

    fun getEvent(eventId: String) = repository.getEvent(eventId)

    fun downloadEvent(eventId: String) = repository.downloadEvent(eventId)

    fun getUser(): LiveData<Map<String, Any>>? {
        val uid = FirebaseManager.getUser()?.uid
        return if(uid != null) generalRepository.getUser(uid) else null
    }

    fun getPlace(id: String) = repository.getPlace(id)

    override fun onCleared() {
        repository.clear()
        super.onCleared()
    }
}
