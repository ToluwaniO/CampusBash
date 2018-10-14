package toluog.campusbash.view.viewmodel

import android.app.Application
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.view.viewmodel.GeneralViewModel

/**
 * Created by oguns on 12/7/2017.
 */
class MainActivityViewModel(app: Application) : GeneralViewModel(app){

    private val db = AppDatabase.getDbInstance(app.applicationContext)

    fun getEvents() = db?.eventDao()?.getEvents()

    fun getUniversities(country: String) = generalRepository.getUnis(country)

}