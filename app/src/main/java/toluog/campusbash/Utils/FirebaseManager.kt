package toluog.campusbash.Utils

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import toluog.campusbash.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import toluog.campusbash.data.EventDataSource
import toluog.campusbash.data.EventDataSource.Companion.db
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask


/**
 * Created by oguns on 12/13/2017.
 */
class FirebaseManager(){
    var db: FirebaseFirestore? = null
    init {
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    fun addEvent(event: Event) {
        Log.d(TAG, "addEventCalled")
        val eventRef = db?.collection(AppContract.FIREBASE_EVENTS)
        if(TextUtils.isEmpty(event.eventId)){
            val document = eventRef?.document()
            event.eventId = document?.id ?: ""
            document?.set(event)
        } else{
            db?.collection(AppContract.FIREBASE_EVENTS)?.document(event.eventId)?.set(event)
        }
        //db?.collection(AppContract.FIREBASE_EVENTS)?.add(event)
    }

    fun uploadEventImage(uri: Uri): UploadTask? {
        // Create a storage reference from our app
        val storageRef = storage?.getReference()

        // Create a reference to "mountains.jpg"
        val imagesRef = storageRef?.child("images")

        return imagesRef?.putFile(uri)
    }

    companion object {
        var storage: FirebaseStorage? = null
        private val TAG = FirebaseManager::class.java.simpleName

    }
}