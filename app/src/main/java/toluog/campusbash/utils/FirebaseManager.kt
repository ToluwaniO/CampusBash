package toluog.campusbash.utils

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import toluog.campusbash.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.jetbrains.anko.toast
import toluog.campusbash.model.Creator


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
        Log.d(TAG, "addEventCalled $event")
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

    fun updateFcmToken(context: Context) {
        val savedStatus = Util.getPrefInt(context, AppContract.PREF_FCM_TOKEN_UPDATED)
        val token = FirebaseInstanceId.getInstance().token
        if(token != null && savedStatus == 0) {
            updateFcmToken(context, token)
        }
    }

    private fun updateFcmToken(context: Context, token: String) {
        Log.d(TAG, "Attempting to upload fcm token")
        val uid = FirebaseManager.getUser()?.uid
        if(uid != null) {
            db?.collection(AppContract.FIREBASE_USERS)?.document(uid)
                    ?.update(AppContract.FIREBASE_FCM_TOKEN, token)
                    ?.addOnSuccessListener {
                        Log.d(TAG, "Fcm token updated")
                        Util.setPrefInt(context, AppContract.PREF_FCM_TOKEN_UPDATED, 1)
                    }
                    ?.addOnFailureListener {
                        Log.d(TAG, "Couldn't save FCM token\ne -> ${it.message}")
                    }
        } else {
            Log.d(TAG, "Can't update fcm token, user is not signed in")
        }
    }

    fun deleteEvent(context: Context?, eventId: String) {
        db?.collection(AppContract.FIREBASE_EVENTS)?.document(eventId)?.delete()
                ?.addOnSuccessListener {
                    context?.toast("Event deleted")
                }?.addOnFailureListener {
                    context?.toast("Event could not be deleted")
                    Log.d(TAG, "An error occurred\n${it.message}")
                }
    }

    fun uploadEventImage(uri: Uri): UploadTask? {
        // Create a storage reference from our app
        val storageRef = storage?.reference
        val user = getUser()

        if(user != null) {
            // Create a reference to "mountains.jpg"
            val imagesRef = storageRef?.child(AppContract.FIREBASESTORAGE_EVENT_IMAGE_PLACEHOLDERS)
                    ?.child(user.uid)
                    ?.child("${uri.lastPathSegment}${System.currentTimeMillis()}")

            return imagesRef?.putFile(uri)
        }
        return null
    }

    fun updateProfileField(key: String, value: String, user: FirebaseUser) {
        db?.collection(AppContract.FIREBASE_USERS)?.document(user.uid)?.update(key, value)
    }

    fun uploadProfilePhoto(uri: Uri) {
        // Create a storage reference from our app
        val storageRef = storage?.reference
        val user = getUser()

        if(user != null) {
            // Create a reference to "mountains.jpg"
            val imagesRef = storageRef?.child(AppContract.FIREBASESTORAGE_PROFILE_PHOTOS)
                    ?.child(user.uid)

            imagesRef?.putFile(uri)?.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                if(taskSnapshot != null) {
                    updateProfileField(AppContract.FIREBASE_USER_PHOTO_URL, taskSnapshot.downloadUrl?.toString() ?: "", user)
                }
            }
        }
    }

    fun buyTicket(event: Event?, map: Map<String, Any>): Task<Void>?{
        if(event == null) return null
        return db?.collection(AppContract.FIREBASE_EVENTS)?.document(event.eventId)?.collection("tickets")
                ?.document()?.set(map)
    }

    companion object {
        var storage: FirebaseStorage? = null
        val auth = FirebaseAuth.getInstance()
        private val TAG = FirebaseManager::class.java.simpleName

        fun isSignedIn() = auth.currentUser != null

        fun getUser() = auth.currentUser

        fun signOut() {
            auth.signOut()
            CampusBash.endCustomerSession()
        }

        fun getFcmToken() = FirebaseInstanceId.getInstance().token

        fun getCreator(): Creator?{
            val user = auth.currentUser
            val name = user?.displayName
            if(user == null) return null
            else if(name == null) return Creator("Anonymous", user.photoUrl.toString(), user.uid)
            else return Creator(name, user.photoUrl.toString(), user.uid)
        }

    }
}