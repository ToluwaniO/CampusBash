package toluog.campusbash.data

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import toluog.campusbash.model.CallableResponse
import toluog.campusbash.model.fromMap

class CloudFunctions {

    private val running = HashSet<String>()
    private val functions = FirebaseFunctions.getInstance()
    private val TAG = CloudFunctions::class.java.simpleName

    fun followUser(uid: String) {
        if (running.contains(FOLLOW_USER)) return
        running.add(FOLLOW_USER)
        functions.getHttpsCallable(FOLLOW_USER).call(mapOf("uid" to uid))
                .continueWith {
                    running.remove(FOLLOW_USER)
                    if (!it.isSuccessful) return@continueWith
                    val map = it.result.data as Map<String, Any?>
                    val result = CallableResponse().fromMap(map)
                    Log.d(TAG, "$result")
                }
    }

    fun unfollowUser(uid: String) {
        if (running.contains(UNFOLLOW_USER)) return
        running.add(UNFOLLOW_USER)
        functions.getHttpsCallable(UNFOLLOW_USER).call(mapOf("uid" to uid))
                .continueWith {
                    running.remove(UNFOLLOW_USER)
                    if (!it.isSuccessful) return@continueWith
                    val map = it.result.data as Map<String, Any?>
                    val result = CallableResponse().fromMap(map)
                    Log.d(TAG, "$result")
                }
    }

    fun blockUser(uid: String) {
        if (running.contains(BLOCK_USER)) return
        running.add(BLOCK_USER)
        functions.getHttpsCallable(BLOCK_USER).call(mapOf("uid" to uid))
                .continueWith {
                    running.remove(BLOCK_USER)
                    if (!it.isSuccessful) return@continueWith
                    val map = it.result.data as Map<String, Any?>
                    val result = CallableResponse().fromMap(map)
                    Log.d(TAG, "$result")
                }
    }

    fun unblockUser(uid: String) {
        if (running.contains(UNBLOCK_USER)) return
        running.add(UNBLOCK_USER)
        functions.getHttpsCallable(UNBLOCK_USER).call(mapOf("uid" to uid))
                .continueWith {
                    running.remove(UNBLOCK_USER)
                    if (!it.isSuccessful) return@continueWith
                    val map = it.result.data as Map<String, Any?>
                    val result = CallableResponse().fromMap(map)
                    Log.d(TAG, "$result")
                }
    }

    companion object {
        const val FOLLOW_USER = "followUser"
        const val UNFOLLOW_USER = "unfollowUser"
        const val BLOCK_USER = "blockUser"
        const val UNBLOCK_USER = "unblockUser"
    }
}