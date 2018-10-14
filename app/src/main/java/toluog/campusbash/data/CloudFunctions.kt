package toluog.campusbash.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import toluog.campusbash.model.CallableResponse
import toluog.campusbash.model.fromMap
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CloudFunctions {

    private val running = HashSet<String>()
    private val functions = FirebaseFunctions.getInstance()
    private val TAG = CloudFunctions::class.java.simpleName

    suspend fun followUser(uid: String) {
        if (running.contains(FOLLOW_USER)) return
        running.add(FOLLOW_USER)
        val task = functions.getHttpsCallable(FOLLOW_USER).call(mapOf("uid" to uid))
        val callback: (HttpsCallableResult) -> Unit = { httpResult ->
                running.remove(FOLLOW_USER)
                val map = httpResult.data as Map<String, Any?>
                val result = CallableResponse().fromMap(map)
                Log.d(TAG, "$result")
        }
        runCallback(task, callback)
    }

    suspend fun unfollowUser(uid: String) {
        if (running.contains(UNFOLLOW_USER)) return
        running.add(UNFOLLOW_USER)
        val task = functions.getHttpsCallable(UNFOLLOW_USER).call(mapOf("uid" to uid))
        val callback: (HttpsCallableResult) -> Unit = {
            running.remove(UNFOLLOW_USER)
            val map = it.data as Map<String, Any?>
            val result = CallableResponse().fromMap(map)
            Log.d(TAG, "$result")
        }
        runCallback(task, callback)
    }

    suspend fun blockUser(uid: String) {
        if (running.contains(BLOCK_USER)) return
        running.add(BLOCK_USER)
        val task = functions.getHttpsCallable(BLOCK_USER).call(mapOf("uid" to uid))
        val callback: (HttpsCallableResult) -> Unit = {
            running.remove(BLOCK_USER)
            val map = it.data as Map<String, Any?>
            val result = CallableResponse().fromMap(map)
            Log.d(TAG, "$result")
        }
        runCallback(task, callback)
    }

    suspend fun unblockUser(uid: String) {
        if (running.contains(UNBLOCK_USER)) return
        running.add(UNBLOCK_USER)
        val task = functions.getHttpsCallable(UNBLOCK_USER).call(mapOf("uid" to uid))
        val callback: (HttpsCallableResult) -> Unit = {
            running.remove(UNBLOCK_USER)
            val map = it.data as Map<String, Any?>
            val result = CallableResponse().fromMap(map)
            Log.d(TAG, "$result")
        }
        runCallback(task, callback)
    }

    private suspend fun <T> runCallback(task: Task<HttpsCallableResult>,
                                        callbackAction: (HttpsCallableResult) -> T): T {
        return suspendCoroutine { continuation ->
            task.addOnSuccessListener {
                continuation.resume(callbackAction(it))
            }.addOnFailureListener {
                continuation.resumeWithException(it)
            }
        }
    }

    companion object {
        const val FOLLOW_USER = "followUser"
        const val UNFOLLOW_USER = "unfollowUser"
        const val BLOCK_USER = "blockUser"
        const val UNBLOCK_USER = "unblockUser"
    }
}