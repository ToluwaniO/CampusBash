package toluog.campusbash.view

import android.app.ProgressDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_stripe_setup.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import toluog.campusbash.BuildConfig
import toluog.campusbash.R
import toluog.campusbash.data.network.ServerResponse
import toluog.campusbash.utils.Util
import toluog.campusbash.view.viewmodel.StripeSetupViewModel

class StripeSetupActivity : AppCompatActivity() {

    private lateinit var viewModel: StripeSetupViewModel
    private val TAG = StripeSetupActivity::class.java.simpleName
    private val STRIPE_URL = "https://stripe.com/ca"
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
            if(Util.isConnected(this@StripeSetupActivity)) {
                setupStripe()
            } else {
                snackbar(root, R.string.no_internet)
            }
        }

        val info = SpannableString(getString(R.string.stripe_setup_info))
        val len = info.length
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View?) {
                val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(STRIPE_URL))
                startActivity(myIntent)
            }
        }
        info.setSpan(clickableSpan, len-5, len-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        message.text = info
        message.movementMethod = LinkMovementMethod.getInstance()
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
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(STRIPE_CONNECT_ACCOUNT_URL)
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
