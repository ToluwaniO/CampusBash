package toluog.campusbash.view

import android.app.ProgressDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.MenuItem
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_stripe_setup.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import toluog.campusbash.BuildConfig
import toluog.campusbash.R
import toluog.campusbash.data.network.ServerResponse
import toluog.campusbash.utils.AppContract

class StripeSetupActivity : AppCompatActivity() {

    private lateinit var viewModel: StripeSetupViewModel
    private val TAG = StripeSetupActivity::class.java.simpleName
    private var stripeResult: LiveData<ServerResponse>? = null
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stripe_setup)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                if(it.status == ACCOUNT_CREATED) {
                    toast(R.string.stripe_account_created)
                    finish()
                } else if(it.status == ACCOUNT_EXISTS) {
                    accountExistsDialog()
                } else {
                    toast(R.string.stripe_account_failed)
                    finish()
                }
            }
        })
    }

    private fun accountExistsDialog() {
        alert(R.string.stripe_account_exists_connect) {
            positiveButton(R.string.yes) {
                startActivity(intentFor<WebActivity>().apply {
                    putExtra(AppContract.WEB_VIEW_URL, STRIPE_CONNECT_ACCOUNT_URL)
                })
                finish()
            }
            negativeButton(R.string.no) {
                finish()
            }
        }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    companion object {
        private val CLIENT_ID = if(BuildConfig.FLAVOR.equals("dev")) {
            "ca_CZDCdiBIFm2webGK1uZavYZH0bcmFBgR"
        } else {
            "ca_CZDCW0OVIvEazokpluKtoXH0VR6QPhJF"
        }
        private const val ACCOUNT_CREATED = 200
        private const val ACCOUNT_EXISTS = 422
        private const val UNKNOWN_ERROR = 400
        private val STRIPE_CONNECT_ACCOUNT_URL = "https://connect.stripe.com/oauth/authorize?response_" +
                "type=code&client_id=$CLIENT_ID&scope=read_write"
    }
}
