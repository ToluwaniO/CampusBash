package toluog.campusbash.view

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import kotlinx.android.synthetic.main.activity_card_payment.*
import org.jetbrains.anko.design.snackbar
import toluog.campusbash.BuildConfig
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import java.lang.Exception
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.stripe.android.CustomerSession
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.credit_card_view.*
import org.jetbrains.anko.*
import toluog.campusbash.utils.CampusBash
import toluog.campusbash.utils.Util
import java.util.*
import kotlin.collections.ArrayList


class CardPaymentActivity : AppCompatActivity() {

    private val TAG = CardPaymentActivity::class.java.simpleName
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 3731
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var googlePayAlert: AlertBuilder<AlertDialog>
    private lateinit var currency: String
    private val cards = ArrayList<BashCard>()
    private val adapter = CardAdapter()
    private lateinit var pleaseWait: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_payment)
        currency = intent.extras.getString(AppContract.CURRENCY)

        if(CampusBash.stripeSessionStarted) {
            Log.d(TAG, "Stripe session started")
            CustomerSession.getInstance().cachedCustomer?.sources?.forEach {
                val card = it.asCard()
                if(card != null) {
                    cards.add(BashCard(card))
                }
            }
            Log.d(TAG, "Found ${cards.size} cached cards")
            updateView()
        }

        pleaseWait = indeterminateProgressDialog(R.string.please_wait)
        pleaseWait.dismiss()
        googlePayAlert = alert(getString(R.string.use_google_pay)) {
            yesButton {
                Log.d(TAG, "Yes clicked for Google Pay")
                val request = createPaymentDataRequest()
                if (request != null) {
                    AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(request),
                            this@CardPaymentActivity, LOAD_PAYMENT_DATA_REQUEST_CODE)
                }
            }
            noButton {
                Log.d(TAG, "User returned no for google pay request")
            }
        }

        val environment = if(BuildConfig.DEBUG) {
            WalletConstants.ENVIRONMENT_TEST
        } else {
            WalletConstants.ENVIRONMENT_PRODUCTION
        }
        paymentsClient = Wallet.getPaymentsClient(this,
                Wallet.WalletOptions.Builder().setEnvironment(environment)
                        .build())

        card_recycler.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        card_recycler.layoutManager = layoutManager

        add_card.setOnClickListener {
            add_card_layout.visibility = View.VISIBLE
            add_card.visibility = View.GONE
            no_card_layout.visibility = View.GONE
        }

        save.setOnClickListener {
            Util.hideKeyboard(this@CardPaymentActivity)
            addCard()
        }

        isReadyToPay()
    }

    override fun onBackPressed() {
        if(add_card_layout.visibility == View.VISIBLE) {
            add_card_layout.visibility = View.GONE
            add_card.visibility = View.VISIBLE
            updateView()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if(data != null) {
                            val paymentData = PaymentData.getFromIntent(data)
                            //val cardInfo = paymentData?.cardInfo
                            paymentData?.paymentMethodToken?.token
                            //val address = paymentData?.shippingAddress
                            val rawToken = paymentData?.paymentMethodToken?.token
                            val stripeToken = Token.fromString(rawToken)
                            if (stripeToken != null) {
                                // This chargeToken function is a call to your own server, which should then connect
                                // to Stripe's API to finish the charge.
                                sendActivityResult(stripeToken.id)
                            }
                        }
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        Log.d(TAG, "Google pay error\ne -> ${status.toString()}")
                    }
                    Activity.RESULT_CANCELED -> Log.d(TAG, "Google Pay result cancelled")
                }
            }
        }
    }

    private fun validateCard(cardToSave: Card): Boolean {
        if(!cardToSave.validateCard()) {
            snackbar(root_view, R.string.could_not_validate_card)
            return false
        }
        return true
    }

    private fun getToken(card: BashCard) {
        pleaseWait.show()
        val stripe = Stripe(this, AppContract.STRIPE_PUBLISHABLE_KEY)
        stripe.createToken(card.card, object : TokenCallback {
            override fun onSuccess(token: Token?) {
                val id: String? = token?.id
                sendActivityResult(id, card.newCard)
            }

            override fun onError(error: Exception?) {
                Log.d(TAG, "Token error\ne -> ${error?.message}")
                sendActivityResult(null)
            }
        })
    }

    private fun sendActivityResult(tokenId: String?, newCard: Boolean = false) {
        val intent = Intent()
        if(tokenId != null) {
            intent.putExtras(Bundle().apply {
                putString(AppContract.TOKEN_ID, tokenId)
                putBoolean(AppContract.NEW_CARD, newCard)
            })
            setResult(Activity.RESULT_OK, intent)
        } else {
            longToast(R.string.could_not_validate_card)
            setResult(Activity.RESULT_CANCELED, intent)
        }
        finish()
    }

    override fun onDestroy() {
        pleaseWait.dismiss()
        super.onDestroy()
    }

    private fun isReadyToPay() {
        val request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build()
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { task ->
            try {
                val result = task.getResult(ApiException::class.java)
                if (result == true) {
                    //show Google as payment option
                    Log.d(TAG, "Showing Google Pay option")
                    googlePayAlert.show()
                } else {
                    //hide Google as payment option
                    Log.d(TAG, "Can't show Google Pay option")
                }
            } catch (exception: ApiException) {
                Log.d(TAG, "Google pay error occurred\ne -> ${exception.message}")
                snackbar(root_view, R.string.error_occurred_g_pay)
            }
        }
    }

    private fun createTokenizationParameters(): PaymentMethodTokenizationParameters {
        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", "stripe")
                .addParameter("stripe:publishableKey", AppContract.STRIPE_PUBLISHABLE_KEY)
                .addParameter("stripe:version", "6.1.2")
                .build()
    }

    private fun createPaymentDataRequest(): PaymentDataRequest {
        val request = PaymentDataRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .setTransactionInfo(TransactionInfo.newBuilder()
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_NOT_CURRENTLY_KNOWN)
                        .setCurrencyCode(currency).build())
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

    private fun updateView() {
        if(cards.size > 0) {
            adapter.notifyDataSetChanged()
            card_recycler.visibility = View.VISIBLE
            no_card_layout.visibility = View.GONE
        } else {
            card_recycler.visibility = View.GONE
            no_card_layout.visibility = View.VISIBLE
        }
    }

    private fun addCard() {
        val card = card_input_widget.card
        if(card != null && validateCard(card)) {
            cards.add(BashCard(card, true))
        } else if(card == null) {
            snackbar(root_view, R.string.could_not_validate_card)
        }
        add_card_layout.visibility = View.GONE
        add_card.visibility = View.VISIBLE
        updateView()
    }

    data class BashCard(var card: Card, var newCard: Boolean = false)

    inner class CardAdapter: RecyclerView.Adapter<CardAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardAdapter.ViewHolder {
            val view = LayoutInflater.from(this@CardPaymentActivity)
                    .inflate(R.layout.credit_card_view, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = cards.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(cards[position])
        }


        inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
                LayoutContainer {

            fun bind(bCard: BashCard) {
                val card = bCard.card
                card_number.text = getString(R.string.debit_card_digits, card.brand, card.last4)
                val logo = Card.BRAND_RESOURCE_MAP[card.brand]
                val unknown = Card.BRAND_RESOURCE_MAP[Card.UNKNOWN]
                if(logo != null) {
                    logo_view.imageResource = logo
                } else if(unknown != null) {
                    logo_view.imageResource = unknown
                }
                containerView?.setOnClickListener {
                    getToken(bCard)
                }
            }

        }

    }
}
