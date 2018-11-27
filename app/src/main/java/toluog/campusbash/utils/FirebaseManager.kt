package toluog.campusbash.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.firestore.DocumentReference
import toluog.campusbash.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONObject
import toluog.campusbash.R
import toluog.campusbash.model.Creator
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.extension.toast
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Created by oguns on 12/13/2017.
 */
class FirebaseManager {
    var db: FirebaseFirestore? = null
    private val threadJob = Dispatchers.IO
    private val threadScope = CoroutineScope(threadJob)
    init {
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    suspend fun addEvent(event: Event): FirebaseOperationResult {
        Log.d(TAG, "addEventCalled $event")
        val eventRef = db?.collection(AppContract.FIREBASE_EVENTS)
        return suspendCoroutine { continuation ->
            val document = if (event.eventId.isBlank()) {
                eventRef?.document()
            } else {
                db?.collection(AppContract.FIREBASE_EVENTS)?.document(event.eventId)
            }
            event.eventId = document?.id ?: ""
            document?.set(event)
                    ?.addOnSuccessListener {
                        continuation.resume(FirebaseOperationResult.Success)
                    }
                    ?.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            ?: FirebaseOperationResult.Error(Exception("An error occurred"))
        }
    }

    suspend fun addTickets(tickets: ArrayList<Ticket>, eventId: String): FirebaseOperationResult {
        Log.d(TAG, "addTicketsCalled $tickets")
        val toWait = arrayListOf<Deferred<Unit>>()
        for (ticket in tickets) {
            if (ticket.ticketId.isNotBlank()) continue
            val ref = db?.collection(AppContract.FIREBASE_EVENTS)?.document(eventId)
                    ?.collection(AppContract.FIREBASE_EVENT_TICKETS)?.document()
            if (ref != null) {
                ticket.apply {
                    this.ticketId = ref.id
                    this.eventId = eventId
                }
                val def = threadScope.async { uploadTickets(ref, ticket) }
                toWait.add(def)
            }
        }
        for (d in toWait) d.await()
        return FirebaseOperationResult.Success
    }

    private suspend fun uploadTickets(ref: DocumentReference, ticket: Ticket) {
        return suspendCoroutine { continuation ->
            ref.set(ticket).addOnSuccessListener {
                continuation.resume(Unit)
            }.addOnFailureListener {
                continuation.resumeWithException(it)
            }
        }
    }

    fun updateFcmToken(context: Context) {
        val savedStatus = Util.getPrefInt(context, AppContract.PREF_FCM_TOKEN_UPDATED)
        val token = FirebaseInstanceId.getInstance().token
        if(token != null && savedStatus == 0) {
            updateFcmToken(context, token)
        }
    }

    fun updateTicketField(eventId: String, ticketId: String, key: String, value: Any, code: String): Task<Task<Void>>? {
        val ref = db?.collection(AppContract.FIREBASE_EVENTS)?.document(eventId)
                ?.collection(AppContract.FIREBASE_EVENT_TICKET)
                ?.document(ticketId)
        return db?.runTransaction {
            ref?.let {ref ->
                val snapshot =  it.get(ref)
                val ticketCodes = snapshot.data?.get(key) as List<HashMap<String, Any>>?
                ticketCodes?.forEach {
                    if(it["code"] as String? == code) {
                        it["isUsed"] = value
                    }
                }
                ref.update(key, ticketCodes)
            }
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
                    context?.toast(R.string.event_deleted)
                }?.addOnFailureListener {
                    context?.toast(R.string.event_could_not_be_deleted)
                    Log.d(TAG, "An error occurred\n${it.message}")
                }
    }

    fun uploadEventImage(uri: Uri): Pair<StorageReference?, UploadTask?> {
        // Create a storage reference from our app
        val storageRef = storage?.reference
        val user = getUser()

        if(user != null) {
            // Create a reference to "mountains.jpg"
            val imagesRef = storageRef?.child(AppContract.FIREBASESTORAGE_EVENT_IMAGE_PLACEHOLDERS)
                    ?.child(user.uid)
                    ?.child("${uri.lastPathSegment}${System.currentTimeMillis()}")

            return Pair(imagesRef, imagesRef?.putFile(uri))
        }
        return Pair(null, null)
    }

    fun updateProfileField(key: String, value: Any, user: FirebaseUser) {
        db?.collection(AppContract.FIREBASE_USERS)?.document(user.uid)?.update(key, value)
    }

    fun uploadProfilePhoto(uri: Uri) {
        // Create a storage reference from our app
        val storageRef = storage?.reference
        val user = getUser()

        if(user != null) {
            // Create a reference to "mountains.jpg"
            val imagesRef = storageRef?.child(AppContract.FIREBASESTORAGE_PROFILE_PHOTOS)
                    ?.child(user.uid)?.child("${System.currentTimeMillis()}")
//
            val uploadTask = imagesRef?.putFile(uri)
            uploadTask?.continueWithTask { task ->
                if(!task.isSuccessful) {
                    Log.d(TAG, task.exception?.message)
                    throw task.exception ?: Exception("Upload task was not successful")
                }
                imagesRef.downloadUrl
            }?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    updateProfileField(AppContract.FIREBASE_USER_PHOTO_URL, task.result.toString(), user)
                } else {
                    Log.d(TAG, task.exception?.message)
                }
            }
        }
    }

    fun buyTicket(event: Event?, map: Map<String, Any>): Task<Void>?{
        if(event == null) return null
        return db?.collection(AppContract.FIREBASE_EVENTS)?.document(event.eventId)?.collection("tickets")
                ?.document()?.set(map)
    }

    sealed class FirebaseOperationResult {
        object Success: FirebaseOperationResult()
        data class Error(val exception: Exception): FirebaseOperationResult()
    }

    companion object {
        private var storage: FirebaseStorage? = null
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        private val functions = FirebaseFunctions.getInstance()
        private val TAG = FirebaseManager::class.java.simpleName

        fun isSignedIn() = auth.currentUser != null

        fun getUser() = auth.currentUser

        fun signOut() {
            auth.signOut()
            CampusBash.endCustomerSession()
        }

        //fun getAuthToken() = getUser()?.getIdToken(true)

        fun getFcmToken() = FirebaseInstanceId.getInstance().token

        fun getCreator(): Creator?{
            val user = auth.currentUser
            val name = user?.displayName
            return when {
                user == null -> null
                name == null -> Creator("Anonymous", user.photoUrl.toString(), user.uid)
                else -> Creator(name, user.photoUrl.toString(), user.uid)
            }
        }

        fun isNewStudentId(studentId: String): Task<String> {
            val data = mapOf(
                    "studentId" to studentId
            )
            return functions.getHttpsCallable("isNewStudentId").call(data)
                    .continueWith { task ->
                        try {
                            if (!task.isSuccessful) return@continueWith JSONObject().toString()
                            val map = task.result?.data as Map<String, Any?>? ?: return@continueWith JSONObject().toString()
                            Log.d(TAG, map.toString())
                            val json = JSONObject()
                            for (key in map.keys) {
                                val value = map[key]
                                if (value != null) {
                                    json.put(key, map[key])
                                }
                            }
                            return@continueWith json.toString()
                        } catch (e: Exception) {
                            return@continueWith JSONObject().toString()
                        }
                    }
        }

        suspend fun getAuthToken(): String {
            return suspendCoroutine { continuation ->
                getUser()?.getIdToken(true)?.addOnSuccessListener {
                    continuation.resume(it.token ?: "")
                }?.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
            }
        }

        suspend fun getDynamicLinkData(intent: Intent): PendingDynamicLinkData {
            return suspendCoroutine { continuation ->
                FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
                        ?.addOnSuccessListener {
                            continuation.resume(it)
                        }
                        ?.addOnFailureListener {
                            continuation.resumeWithException(it)
                        }
            }
        }

        suspend fun getShortDynamicLink(url: String): ShortDynamicLink? {
            return suspendCoroutine { continuation ->
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLongLink(Uri.parse(url))
                        .buildShortDynamicLink()
                        ?.addOnSuccessListener {
                            continuation.resume(it)
                        }
                        ?.addOnFailureListener {
                            Log.e(TAG, it.message)
                            continuation.resume(null)
                        }
            }
        }

    }
}