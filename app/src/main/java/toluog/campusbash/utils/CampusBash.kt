package toluog.campusbash.utils

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.android.CustomerSession
import toluog.campusbash.data.GeneralDataSource.Companion.user
import toluog.campusbash.data.ProgressListener
import toluog.campusbash.data.Repository
import toluog.campusbash.data.StripeEphemeralKeyProvider

object CampusBash {

    private val TAG = CampusBash::class.java.simpleName
    private const val MAX_CUSTOMER_SESSION_RETRIES = 3
    private lateinit var user: LiveData<Map<String, Any>>
    private var customerSessionRetries = 0
    var stripeSessionStarted = false
    private set(value) {
        field = value
    }

    fun init(c: Context) {
        val repo = Repository(c, FirebaseFirestore.getInstance())
        val uid = FirebaseManager.auth.uid
        uid?.let {
            user = repo.getUser(it)
            user.observeForever {
                it?.let {
                    initCustomerSession(it["stripeCustomerId"] as String?)
                }
            }
        }
    }

    fun initCustomerSession(customerId: String?) {
        Log.d(TAG,"Initializing customer session")
        if(!stripeSessionStarted && customerId != null
                && customerSessionRetries < MAX_CUSTOMER_SESSION_RETRIES){
            CustomerSession.initCustomerSession(StripeEphemeralKeyProvider(object : ProgressListener {
                override fun onStringResponse(message: String) {
                    if(!message.startsWith("Error:")) {
                        stripeSessionStarted = true
                        Log.d(TAG, "Stripe session started")
                    } else {
                        customerSessionRetries++
                        initCustomerSession(customerId)
                        Log.d(TAG, "Failed to start stripe session")
                    }
                }
            }, customerId))
        }
    }

    fun endCustomerSession() {
        CustomerSession.endCustomerSession()
        stripeSessionStarted = false
    }
}