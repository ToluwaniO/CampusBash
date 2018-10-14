package toluog.campusbash.view.viewmodel

import android.app.Application

/**
 * Created by oguns on 12/28/2017.
 */
class FirstOpenViewModel(app: Application): GeneralViewModel(app){

    fun getUniversities(country: String) = generalRepository.getUnis(country)

}