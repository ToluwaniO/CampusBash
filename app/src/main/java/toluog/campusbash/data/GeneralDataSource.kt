package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class GeneralDataSource {
    companion object {

        var db: AppDatabase? = null
        val TAG = GeneralDataSource::class.java.simpleName
        val user = MutableLiveData<Map<String, Any>>()
        lateinit var userQuery: DocumentReference

        fun getUser(mFirestore: FirebaseFirestore, uid: String): LiveData<Map<String, Any>> {
            Log.d(TAG, "getting user")
            userQuery = mFirestore.collection(FirestorePaths.USERS).document(uid)
            userQuery.addSnapshotListener({ documentSnapshot, err ->
                if(documentSnapshot != null && documentSnapshot.exists()) {
                    user.postValue(documentSnapshot.data)
                }
                if(err != null) {
                    Log.d(TAG, "An error occurred getting user\ne -> ${err.message}")
                }
            })
            return user
        }
    }
}