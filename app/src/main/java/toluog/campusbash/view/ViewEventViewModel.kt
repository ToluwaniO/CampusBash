package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.data.Repository
import toluog.campusbash.utils.FirebaseManager

/**
 * Created by oguns on 12/23/2017.
 */
class ViewEventViewModel(private val app: Application): GeneralViewModel(app){

    fun getEvent(eventId: String) = repo.getEvent(eventId)

    fun downloadEvent(eventId: String) {
        repo.downloadEvent(eventId, app.applicationContext)
    }

    fun getUser(): LiveData<Map<String, Any>>? {
        val uid = FirebaseManager.getUser()?.uid
        return if(uid != null) repo.getUser(uid) else null
    }

    fun getPlace(id: String) = repo.getPlace(id)
}