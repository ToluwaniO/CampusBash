package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.Repository

/**
 * Created by oguns on 12/7/2017.
 */
class MainActivityViewModel(app: Application) : GeneralViewModel(app){

    val db: AppDatabase?

    init {
        db = AppDatabase.getDbInstance(app.applicationContext)
    }

    fun getEvents() = db?.eventDao()?.getEvents()

    fun getUniversities(country: String) = repo.getUnis(country)

}