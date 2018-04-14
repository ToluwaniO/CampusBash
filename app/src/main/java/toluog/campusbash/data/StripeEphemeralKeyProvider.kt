package toluog.campusbash.data

import android.app.Activity
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.util.Log
import com.stripe.android.EphemeralKeyProvider
import com.stripe.android.EphemeralKeyUpdateListener
import toluog.campusbash.data.network.StripeServerClient

class StripeEphemeralKeyProvider(val progressListener: ProgressListener, val customerId: String): EphemeralKeyProvider {

    private val TAG = StripeEphemeralKeyProvider::class.java.simpleName
    private lateinit var stripeServerClient: StripeServerClient
    private lateinit var key: LiveData<String>



    override fun createEphemeralKey(apiVersion: String, keyUpdateListener: EphemeralKeyUpdateListener) {
        Log.d(TAG, "Create ephemeral key called")
        stripeServerClient = StripeServerClient()
        stripeServerClient.createEphemeralKey(customerId, apiVersion)
        key = stripeServerClient.getEphemeralKey()
        key.observeForever(object : Observer<String> {
            override fun onChanged(it: String?) {
                if(it != null) {
                    keyUpdateListener.onKeyUpdate(it)
                    progressListener.onStringResponse(it)
                } else {
                    Log.d(TAG, "Ephemeral key is null")
                    progressListener.onStringResponse("Error: Could not get key")
                }
                key.removeObserver(this)
            }

        })
    }

}

interface ProgressListener {
    fun onStringResponse(message: String)
}