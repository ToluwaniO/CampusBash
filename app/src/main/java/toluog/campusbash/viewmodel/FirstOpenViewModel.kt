package toluog.campusbash.viewmodel

import android.app.Application
import toluog.campusbash.viewmodel.GeneralViewModel

/**
 * Created by oguns on 12/28/2017.
 */
class FirstOpenViewModel(app: Application): GeneralViewModel(app){

    fun getUniversities(country: String) = repo.getUnis(country)

}