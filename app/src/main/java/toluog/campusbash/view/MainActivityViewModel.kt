package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.Repository

/**
 * Created by oguns on 12/7/2017.
 */
class MainActivityViewModel(app: Application) : AndroidViewModel(app){

    val repo: Repository
    val db: AppDatabase?

    init {
        repo = Repository(app.applicationContext)
        db = AppDatabase.getDbInstance(app.applicationContext)
    }

    fun getEvents() = db?.eventDao()?.getEvents()

    fun getUniversities() = repo.getUnis("Canada")

}