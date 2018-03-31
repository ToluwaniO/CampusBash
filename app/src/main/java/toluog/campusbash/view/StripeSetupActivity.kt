package toluog.campusbash.view

import android.app.ProgressDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_stripe_setup.*
import org.jetbrains.anko.act
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import toluog.campusbash.R
import toluog.campusbash.data.network.ServerResponse

class StripeSetupActivity : AppCompatActivity() {

    private lateinit var viewModel: StripeSetupViewModel
    private val TAG = StripeSetupActivity::class.java.simpleName
    private var stripeResult: LiveData<ServerResponse>? = null
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stripe_setup)

        viewModel = ViewModelProviders.of(this).get(StripeSetupViewModel::class.java)

        dialog = indeterminateProgressDialog(R.string.please_wait)
        dialog.dismiss()

        create_stripe_button.setOnClickListener {
            setupStripe()
        }
    }

    override fun onPause() {
        dialog.dismiss()
        super.onPause()
    }

    override fun onDestroy() {
        dialog.dismiss()
        super.onDestroy()
    }

    private fun setupStripe() {
        dialog.show()
        stripeResult = viewModel.createStripeAccount()
        stripeResult?.removeObservers(this)
        stripeResult?.observe(this, Observer {
            if(it != null) {
                dialog.dismiss()
                if(it.status == 200) {
                    toast(R.string.stripe_account_created)
                } else {
                    toast(R.string.stripe_account_failed)
                }
                finish()
            }
        })
    }
}
