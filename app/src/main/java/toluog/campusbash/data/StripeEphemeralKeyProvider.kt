package toluog.campusbash.data

import android.util.Log
import com.stripe.android.EphemeralKeyProvider
import com.stripe.android.EphemeralKeyUpdateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import toluog.campusbash.data.network.StripeServerClient

class StripeEphemeralKeyProvider(val progressListener: ProgressListener, val customerId: String, val uid: String): EphemeralKeyProvider {

    private val TAG = StripeEphemeralKeyProvider::class.java.simpleName
    private val MAX_KEY_REQUESTS = 3
    private var requestsMade = 0
    private lateinit var stripeServerClient: StripeServerClient
    private val threadScope = CoroutineScope(Dispatchers.IO)

    override fun createEphemeralKey(apiVersion: String, keyUpdateListener: EphemeralKeyUpdateListener) {
        Log.d(TAG, "Create ephemeral key called")
        if (requestsMade <= MAX_KEY_REQUESTS) {
            requestsMade++
            stripeServerClient = StripeServerClient()
            updateKey(apiVersion, keyUpdateListener)
        }
    }

    private fun updateKey(apiVersion: String, keyUpdateListener: EphemeralKeyUpdateListener) {
        threadScope.launch {
            try {
                val key = stripeServerClient.createEphemeralKey(customerId, apiVersion)
                Log.d(TAG, key)
                keyUpdateListener.onKeyUpdate(key)
                progressListener.onStringResponse(key)
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                progressListener.onStringResponse("Error: Could not get key")
            }

        }
    }

}

interface ProgressListener {
    fun onStringResponse(message: String)
}