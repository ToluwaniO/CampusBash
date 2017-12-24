package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import toluog.campusbash.data.Repository

/**
 * Created by oguns on 12/23/2017.
 */
class ViewEventViewModel(app: Application): AndroidViewModel(app){
    val repo = Repository(app.applicationContext)

    fun getEvent(eventId: String) = repo.getEvent(eventId)

}