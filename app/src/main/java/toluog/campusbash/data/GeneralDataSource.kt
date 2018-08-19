package toluog.campusbash.data

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.places.Places
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.doAsync
import toluog.campusbash.model.Event
import java.util.concurrent.Executors

object GeneralDataSource {

    private var db: AppDatabase? = null
    private val TAG = GeneralDataSource::class.java.simpleName
    private val user = MutableLiveData<Map<String, Any>>()
    private lateinit var userQuery: DocumentReference
    private var lastUid: String? = null
    private var listenerRegistration: ListenerRegistration? = null
    private var dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun getUser(mFirestore: FirebaseFirestore, uid: String): LiveData<Map<String, Any>> {
        Log.d(TAG, "getting user")
        if(lastUid != uid) {
            listenerRegistration?.remove()
            userQuery = mFirestore.collection(FirestorePaths.USERS).document(uid)
            listenerRegistration = userQuery.addSnapshotListener { documentSnapshot, err ->
                launch(dispatcher) {
                    if(documentSnapshot != null && documentSnapshot.exists()) {
                        user.postValue(documentSnapshot.data)
                    }
                    if(err != null) {
                        Log.d(TAG, "An error occurred getting user\ne -> ${err.message}")
                    }
                }
            }
            lastUid = uid
        }
        return user
    }

    @SuppressLint("RestrictedApi")
    fun fetchPlace(id: String, event: Event, context: Context) {
        db = AppDatabase.getDbInstance(context)
        val geoClient = Places.getGeoDataClient(context)
        geoClient.getPlaceById(id).addOnCompleteListener {task ->
            if(task.isSuccessful) {
                val places =  task.result
                val myPlace = places[0]
                val place = toluog.campusbash.model.Place().apply {
                    this.id = myPlace.id
                    this.address = myPlace.address.toString()
                    this.latLng.lat = myPlace.latLng.latitude
                    this.latLng.lon = myPlace.latLng.longitude
                    this.name = myPlace.name.toString()
                }
                doAsync {
                    db?.placeDao()?.insertPlace(place)
                    db?.eventDao()?.updateEvent(event.apply {
                        address = place.address
                    })
                }
                Log.i(TAG, "Place found: " + myPlace.name)
                places.release()
            } else {
                Crashlytics.log("$TAG -> Could not get place\n${task.exception?.message}")
            }
        }
    }

}