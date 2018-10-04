package toluog.campusbash.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import toluog.campusbash.model.University

class SelectUniversityViewModel(app: Application): GeneralViewModel(app) {
    fun getUniversities() = repo.getUniversities()

    fun getUniversities(query: String) = repo.getUniversities(query)
}