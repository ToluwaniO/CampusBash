package toluog.campusbash.view.viewmodel

import android.app.Application

class SelectUniversityViewModel(app: Application): GeneralViewModel(app) {
    fun getUniversities() = generalRepository.getUniversities()
    fun getUniversities(query: String) = generalRepository.getUniversities(query)
}