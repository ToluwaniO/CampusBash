package toluog.campusbash.view

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import org.jetbrains.anko.*
import java.util.*


class CardPaymentActivity : AppCompatActivity() {

    private val TAG = CardPaymentActivity::class.java.simpleName
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 3731
    private lateinit var dialog: ProgressDialog
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var googlePayAlert: AlertBuilder<AlertDialog>
    private lateinit var currency: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_payment)
        currency = intent.extras.getString("currency")

        dialog = indeterminateProgressDialog(message = "Please wait…")
        dialog.dismiss()
        googlePayAlert = alert("Do you want to use Google Pay?") {
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

        done_button.setOnClickListener {
            val card = card_input_widget.card
            if(card != null && validateCard(card)) {
                dialog.show()
                getToken(card)
            }
        }

        isReadyToPay()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val paymentData = PaymentData.getFromIntent(data)
                        val cardInfo = paymentData?.cardInfo
                        val address = paymentData?.shippingAddress
                        val rawToken = paymentData?.paymentMethodToken?.token
                        val stripeToken = Token.fromString(rawToken)
                        if (stripeToken != null) {
                            // This chargeToken function is a call to your own server, which should then connect
                            // to Stripe's API to finish the charge.
                            sendActivityResult(stripeToken.id)
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

    private fun validateCard(cardToSave: Card?): Boolean {
        if(cardToSave == null) {
            snackbar(root_view, "Invalid card")
            return false
        } else if(!cardToSave.validateCard()) {
            snackbar(root_view, "Invalid card")
            return false
        }
        return true
    }

    private fun getToken(card: Card) {
        val stripe = Stripe(this, AppContract.STRIPE_PUBLISHABLE_KEY)
        stripe.createToken(card, object : TokenCallback {
            override fun onSuccess(token: Token?) {
                val id: String? = token?.id
                sendActivityResult(id)
            }

            override fun onError(error: Exception?) {
                Log.d(TAG, "Token error\ne -> ${error?.message}")
                sendActivityResult(null)
            }
        })
    }

    private fun sendActivityResult(tokenId: String?) {
        val intent = Intent()
        if(tokenId != null) {
            intent.putExtras(Bundle().apply {
                putString(AppContract.TOKEN_ID, tokenId)
            })
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED, intent)
        }
        dialog.dismiss()
        finish()
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
                snackbar(root_view, "An error occurred by Google Pay")
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
}
