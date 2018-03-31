package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.data.Repository

class StripeSetupViewModel(app: Application) : AndroidViewModel(app)  {

    private val repo: Repository = Repository(app.applicationContext, FirebaseFirestore.getInstance())
    private val TAG = EventsViewModel::class.java.simpleName

    fun createStripeAccount() = repo.createStripeAccount()

}