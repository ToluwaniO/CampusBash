package toluog.campusbash.view.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import toluog.campusbash.data.network.ServerResponse

class StripeSetupViewModel(app: Application) : GeneralViewModel(app)  {

    private val TAG = EventsViewModel::class.java.simpleName

    fun createStripeAccount(): LiveData<ServerResponse> {
        val live = MutableLiveData<ServerResponse>()
        this.launch {
            live.postValue(generalRepository.createStripeAccount())
        }
        return live
    }
}