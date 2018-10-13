package toluog.campusbash.view.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import toluog.campusbash.data.datasource.UniversityDataSource
import toluog.campusbash.data.repository.GeneralRepository
import kotlin.coroutines.CoroutineContext

open class GeneralViewModel(val app: Application): AndroidViewModel(app), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
    val generalRepository = GeneralRepository(app.applicationContext, coroutineContext)
    private var lastUser = ""
    private var profileInfo: LiveData<Map<String, Any>>? = null

    fun getProfileInfo(user: FirebaseUser): LiveData<Map<String, Any>>? {
        if(user.uid != lastUser || profileInfo == null) {
            profileInfo = generalRepository.getUser(user.uid)
        }
        return profileInfo
    }

    fun getFroshGroup(): LiveData<Set<String>> = generalRepository.getFroshGroup()

    fun listenForUniversities() = UniversityDataSource(app.applicationContext, coroutineContext).listenToUniversities()

    override fun onCleared() {
        super.onCleared()
        generalRepository.clear()
        job.cancel()
    }
}