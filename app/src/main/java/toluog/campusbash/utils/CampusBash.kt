package toluog.campusbash.utils

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.stripe.android.CustomerSession
import com.stripe.android.model.Customer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import toluog.campusbash.data.ProgressListener
import toluog.campusbash.data.StripeEphemeralKeyProvider
import toluog.campusbash.data.repository.GeneralRepository
import toluog.campusbash.model.BashCard

object CampusBash {

    private val TAG = CampusBash::class.java.simpleName
    private const val MAX_CUSTOMER_SESSION_RETRIES = 3
    private lateinit var user: LiveData<Map<String, Any>>
    private var isInitialized = false
    private var customerSessionRetries = 0
    var stripeSessionStarted = false
    private set(value) {
        field = value
    }
    private var bashCards = MutableLiveData<List<BashCard>>()
    private val threadScope = CoroutineScope(Dispatchers.Default)

    @SuppressLint("RestrictedApi")
    fun init(c: Context) {
        if(!isInitialized) {
            val repo = GeneralRepository(c, Dispatchers.Default)
            val uid = FirebaseManager.auth.uid
            uid?.let {
                user = repo.getUser(it)
                user.observeForever {
                    Log.d(TAG, "Observing user")
                    it?.let {
                        initCustomerSession(it["stripeCustomerId"] as String?, c)
                    }
                }
            }

            threadScope.launch {
                repo.deleteOldEvents()
                DbManager.deleteInvalidPlaces(c)
            }
            isInitialized = true
        }
    }

    @Synchronized
    fun initCustomerSession(customerId: String?, context: Context) {
        Log.d(TAG,"Initializing customer session")
        if(!stripeSessionStarted && customerId != null
                && customerSessionRetries < MAX_CUSTOMER_SESSION_RETRIES &&  Util.isConnected(context)){
            CustomerSession.initCustomerSession(StripeEphemeralKeyProvider(object : ProgressListener {
                override fun onStringResponse(message: String) {
                    if(!message.startsWith("Error:")) {
                        stripeSessionStarted = true
                        Log.d(TAG, "Stripe session started")
                        retrieveCustomer()
                    } else {
                        customerSessionRetries++
                        initCustomerSession(customerId, context)
                        Log.d(TAG, "Failed to start stripe session")
                    }
                }
            }, customerId, FirebaseManager.getUser()?.uid ?: ""))
        }
    }

    fun endCustomerSession() {
        CustomerSession.endCustomerSession()
        stripeSessionStarted = false
    }

    private fun retrieveCustomer() {
        try {
            Log.d(TAG, "Attempting to retrieve customer")
            CustomerSession.getInstance().retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
                override fun onCustomerRetrieved(customer: Customer) {
                    Log.d(TAG, "${customer.sources.size} cards found")
                    val cards = arrayListOf<BashCard>()
                    customer.sources.forEach {
                        if(it != null) {
                            cards.add(BashCard(it))
                        }
                    }
                    Log.d(TAG, "$cards")
                    bashCards.postValue(cards)
                }

                override fun onError(errorCode: Int, errorMessage: String?) {
                    Log.d(TAG, errorMessage)
                    Crashlytics.log("$TAG ($errorCode) -> $errorMessage")
                }

            })
        } catch (e: IllegalStateException) {
            Log.d(TAG, e.message)
        }
    }

    fun getBashCards() = bashCards
}