package toluog.campusbash.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.webkit.WebView
import toluog.campusbash.utils.AppContract

class WebActivity : AppCompatActivity() {

    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = intent.extras.getString(AppContract.WEB_VIEW_URL)
        val webView = WebView(this)
        webView.loadUrl(url)
        setContentView(webView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }
}
