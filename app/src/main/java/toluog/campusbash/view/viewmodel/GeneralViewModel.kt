package toluog.campusbash.view.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import toluog.campusbash.data.datasource.UniversityDataSource
import toluog.campusbash.data.network.ProfileServerClient
import toluog.campusbash.data.network.ServerResponseState
import toluog.campusbash.data.network.StripeServerClient
import toluog.campusbash.data.repository.GeneralRepository
import toluog.campusbash.model.TicketPriceBreakdown
import toluog.campusbash.utils.FirebaseManager
import kotlin.coroutines.CoroutineContext

open class GeneralViewModel(val app: Application): AndroidViewModel(app), CoroutineScope {
    private val job = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + job)
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default
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

    fun isValidStudentId(studentId: String): LiveData<ServerResponseState>? {
        val uid = FirebaseManager.getUser()?.uid ?: return null
        val liveResponse = MutableLiveData<ServerResponseState>()
        this.launch {
            liveResponse.postValue(ProfileServerClient().isNewStudentId(uid, studentId))
        }
        return liveResponse
    }

    suspend fun getTicketBreakdown(price: Int): TicketPriceBreakdown? {
        val client = StripeServerClient()
        return client.getTicketBreakdown(price)
    }

    override fun onCleared() {
        super.onCleared()
        generalRepository.clear()
        job.cancel()
    }
}