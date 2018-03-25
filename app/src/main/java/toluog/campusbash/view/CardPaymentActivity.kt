package toluog.campusbash.view

import android.app.Activity
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
import org.jetbrains.anko.progressDialog
import toluog.campusbash.BuildConfig
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import java.lang.Exception

class CardPaymentActivity : AppCompatActivity() {

    private val TAG = CardPaymentActivity::class.java.simpleName
    val dialog = progressDialog(message = "Please wait…")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_payment)
        done_button.setOnClickListener {
            val card = card_input_widget.card
            if(card != null && validateCard(card)) {
                dialog.show()
                getToken(card)
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
        val stripe = Stripe(this, BuildConfig.STRIPE_PUBLISHABLE_KEY)
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
            intent.putExtra(AppContract.TOKEN_ID, Bundle().apply {
                putString(AppContract.TOKEN_ID, tokenId)
            })
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED, intent)
        }
        dialog.dismiss()
        finish()
    }
}
