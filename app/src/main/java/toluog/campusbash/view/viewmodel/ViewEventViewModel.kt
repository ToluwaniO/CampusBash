package toluog.campusbash.view.viewmodel

import android.app.Application
import android.util.Log
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import com.google.android.gms.wallet.*
import toluog.campusbash.data.repository.EventsRepository
import toluog.campusbash.model.Event
import toluog.campusbash.model.TicketPriceBreakdown
import toluog.campusbash.model.toMap
import toluog.campusbash.utils.Analytics
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import java.lang.Exception
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by oguns on 12/23/2017.
 */
class ViewEventViewModel(app: Application): GeneralViewModel(app){
    private val TAG = ViewEventViewModel::class.java.simpleName
    private val repository = EventsRepository(app.applicationContext, coroutineContext)
    private val user = getUser()
    private val quantityMap = ArrayMap<String, Any>()
    var event: Event? = null
    var breakdown = TicketPriceBreakdown()
    var currency: String? = null

    fun getEvent(eventId: String) = repository.getEvent(eventId)

    fun downloadEvent(eventId: String) = repository.downloadEvent(eventId)

    fun getUser(): LiveData<Map<String, Any>>? {
        val uid = FirebaseManager.getUser()?.uid
        return if(uid != null) generalRepository.getUser(uid) else null
    }

    fun getPlace(id: String) = repository.getPlace(id)

    fun getTickets(eventId: String) = repository.getEventTickets(eventId)

    fun updateQuantityMap(map: Map<String, Any>) {
        quantityMap.clear()
        quantityMap.putAll(map)
    }

    fun createPaymentDataRequest(): PaymentDataRequest {
        val request = PaymentDataRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .setTransactionInfo(TransactionInfo.newBuilder()
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_NOT_CURRENTLY_KNOWN)
                        .setCurrencyCode(currency ?: "").build())
                .setCardRequirements(
                        CardRequirements.newBuilder()
                                .addAllowedCardNetworks(Arrays.asList(
                                        WalletConstants.CARD_NETWORK_AMEX,
                                        WalletConstants.CARD_NETWORK_DISCOVER,
                                        WalletConstants.CARD_NETWORK_VISA,
                                        WalletConstants.CARD_NETWORK_MASTERCARD))
                                .build())

        request.setPaymentMethodTokenizationParameters(createTokenizationParameters())
        return request.build()
    }

    private fun createTokenizationParameters(): PaymentMethodTokenizationParameters {
        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", "stripe")
                .addParameter("stripe:publishableKey", AppContract.STRIPE_PUBLISHABLE_KEY)
                .addParameter("stripe:version", "7.0.1")
                .build()
    }

    suspend fun buyTickets(tokenId: String?, newCard: Boolean): BuyTicketState {
        val overallMap = getData()
        if(overallMap[AppContract.QUANTITY] == 0){
            return BuyTicketState.QuantityIsZero
        }
        if (breakdown.totalFee > 0 && tokenId == null) {
            return BuyTicketState.NoPaymentMethod
        }
        val customerId = user?.value?.get(AppContract.STRIPE_CUSTOMER_ID) as String?
        val userName = user?.value?.get(AppContract.FIREBASE_USER_USERNAME) as String?
        if(tokenId != null) {
            overallMap[AppContract.TOKEN] = tokenId
            overallMap[AppContract.NEW_CARD] = newCard
        }
        if(customerId != null) {
            overallMap[AppContract.STRIPE_CUSTOMER_ID] = customerId
        }
        if(userName != null) {
            overallMap[AppContract.BUYER_NAME] = userName
        }

        overallMap[AppContract.TIME_SPENT] = System.currentTimeMillis()

        val uid = FirebaseManager.auth.currentUser?.uid
        val email = FirebaseManager.auth.currentUser?.email
        val stripeId = event?.creator?.stripeAccountId
        if(stripeId != null) overallMap[AppContract.STRIPE_ACCOUNT_ID] = stripeId
        if(email != null) overallMap[AppContract.BUYER_EMAIL] = email

        if(uid != null) {
            overallMap[AppContract.BUYER_ID] = uid
            return saveData(overallMap)
        }
        return BuyTicketState.NotSignedIn
    }

    fun getData(): HashMap<String, Any> {
        val purchaseMap = HashMap<String, Any>()
        var totalQuantity = 0
        for (key in quantityMap.keys) {
            val quantity = quantityMap[key] as Int
            totalQuantity += quantity
        }
        if(currency != null) {
            purchaseMap[AppContract.CURRENCY] = currency ?: ""
        }
        purchaseMap[AppContract.TICKETS] = quantityMap
        purchaseMap[AppContract.QUANTITY] = totalQuantity
        purchaseMap[AppContract.TOTAL] = breakdown.totalFee
        purchaseMap[AppContract.BREAKDOWN] = breakdown.toMap()
        return purchaseMap
    }

    private suspend fun saveData(map: Map<String, Any>): BuyTicketState{
        val task = FirebaseManager().buyTicket(event, map)
        Log.d(TAG, "$map")
        return suspendCoroutine { continuation ->
            task?.addOnSuccessListener {
                event?.let { ev -> Analytics.logTicketBought(ev) }
                continuation.resume(BuyTicketState.Success)
            }?.addOnFailureListener {
                event?.let { ev -> Analytics.logTicketBoughtFailed(ev) }
                Log.e(TAG, "Error saving data\nerror -> ${it.message}")
                continuation.resumeWithException(it)
            } ?: continuation.resumeWithException(Exception("task is null"))
        }
    }

    override fun onCleared() {
        repository.clear()
        super.onCleared()
    }

    sealed class BuyTicketState {
        object Success: BuyTicketState()
        object NotSignedIn: BuyTicketState()
        object QuantityIsZero: BuyTicketState()
        object NoPaymentMethod: BuyTicketState()
        data class Error(val exception: Exception): BuyTicketState()
    }
}
