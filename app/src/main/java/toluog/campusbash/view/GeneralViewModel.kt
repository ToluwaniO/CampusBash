package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.data.Repository

open class GeneralViewModel(app: Application): AndroidViewModel(app) {
    val repo = Repository(app.applicationContext, FirebaseFirestore.getInstance())
    private var profileInfo: LiveData<Map<String, Any>>? = null

    fun getProfileInfo(user: FirebaseUser): LiveData<Map<String, Any>>? {
        if(profileInfo == null) {
            profileInfo = repo.getUser(user.uid)
        }
        return profileInfo
    }
}