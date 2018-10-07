package toluog.campusbash.view.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import toluog.campusbash.data.datasource.UniversityDataSource
import toluog.campusbash.data.repository.GeneralRepository

open class GeneralViewModel(val app: Application): AndroidViewModel(app) {
    val generalRepository = GeneralRepository(app.applicationContext)
    private var lastUser = ""
    private var profileInfo: LiveData<Map<String, Any>>? = null

    fun getProfileInfo(user: FirebaseUser): LiveData<Map<String, Any>>? {
        if(user.uid != lastUser || profileInfo == null) {
            profileInfo = generalRepository.getUser(user.uid)
        }
        return profileInfo
    }

    fun getFroshGroup(): LiveData<Set<String>> = generalRepository.getFroshGroup()

    fun listenForUniversities() = UniversityDataSource(app.applicationContext).listenToUniversities()
}