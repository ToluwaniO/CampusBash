package toluog.campusbash.data.datasource

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import toluog.campusbash.data.FirestorePaths
import toluog.campusbash.model.PublicProfile
import kotlin.coroutines.CoroutineContext

class PublicProfileDataSource(override val coroutineContext: CoroutineContext) : DataSource() {

    private val TAG = PublicProfileDataSource::class.java.simpleName
    private var listenerProfileRegistration: ListenerRegistration? = null
    private var listenerFollowerRegistration: ListenerRegistration? = null
    private var listenerFollowingRegistration: ListenerRegistration? = null
    private var lastUid = ""
    val liveProfile = MutableLiveData<PublicProfile?>()
    val liveFollowers = MutableLiveData<List<PublicProfile>>()
    val liveFollowing = MutableLiveData<List<PublicProfile>>()

    fun initListener(uid: String) {
        if (lastUid == uid) return
        lastUid = uid
        clearListeners()
        fetchProfile(uid)
        fetchFollowers(uid)
        fetchFollowing(uid)
    }

    private fun fetchFollowing(uid: String) {
        val query = FirebaseFirestore.getInstance().collection(FirestorePaths.PUBLIC_PROFILE)
                .document(uid).collection(FirestorePaths.FOLLOWING)
        listenerFollowingRegistration = query.addSnapshotListener { querySnapshot, e ->
            launch {
                if (e != null) {
                    Log.d(TAG, e.message)
                    return@launch
                }
                val following = arrayListOf<PublicProfile>()
                querySnapshot?.documents?.forEach {
                    val pr = it.toObject(PublicProfile::class.java)
                    if (pr != null) {
                        following.add(pr)
                    }
                }
                Log.d(TAG, "$following")
                liveFollowing.postValue(following)
            }
        }
    }

    private fun fetchFollowers(uid: String) {
        val query = FirebaseFirestore.getInstance().collection(FirestorePaths.PUBLIC_PROFILE)
                .document(uid).collection(FirestorePaths.FOLLOWERS)
        listenerFollowerRegistration = query.addSnapshotListener { querySnapshot, e ->
            this.launch {
                if (e != null) {
                    Log.d(TAG, e.message)
                    return@launch
                }
                val followers = arrayListOf<PublicProfile>()
                querySnapshot?.documents?.forEach {
                    val pr = it.toObject(PublicProfile::class.java)
                    if (pr != null) {
                        followers.add(pr)
                    }
                }
                Log.d(TAG, "$followers")
                liveFollowers.postValue(followers)
            }
        }
    }

    private fun fetchProfile(uid: String) {
        val query = FirebaseFirestore.getInstance().collection(FirestorePaths.PUBLIC_PROFILE).document(uid)
        listenerProfileRegistration = query.addSnapshotListener { documentSnapshot, err ->
            launch {
                if (err != null) {
                    Log.d(TAG, err.message)
                    return@launch
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val publicProfile = documentSnapshot.toObject(PublicProfile::class.java)
                    Log.d(TAG,"$publicProfile")
                    liveProfile.postValue(publicProfile)
                } else {
                    Log.d(TAG, "document does not exist")
                }
            }
        }
    }

    private fun clearListeners() {
        listenerFollowerRegistration?.remove()
        listenerProfileRegistration?.remove()
        listenerFollowingRegistration?.remove()

        liveProfile.postValue(null)
        liveFollowing.postValue(emptyList())
        liveFollowers.postValue(emptyList())
    }

    override fun clear() {
        clearListeners()
    }
}