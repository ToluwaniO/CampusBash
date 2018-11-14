package toluog.campusbash.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.android.synthetic.main.event_card_layout.*
import org.jetbrains.anko.toast
import toluog.campusbash.BuildConfig
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.loadImage
import toluog.campusbash.view.viewmodel.ViewEventViewModel

class ShareEventActivity : AppCompatActivity() {

    private val TAG = ShareEventActivity::class.java.simpleName
    private lateinit var event: Event
    private lateinit var viewModel: ViewEventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_event)

        supportActionBar?.title = getString(R.string.share_event)
        viewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)

        event = intent.extras?.getParcelable(AppContract.MY_EVENT_BUNDLE) ?: Event()

        viewModel.getPlace(event.placeId)?.observe(this, Observer {
            updateUi(it)
        })
    }

    private fun updateUi(place: Place) {
        val placeholder = event.placeholderImage
        if (placeholder != null && placeholder.url.isNotBlank()) {
            event_image.loadImage(placeholder.url)
        } else {
            event_image.setImageResource(R.drawable.default_event_background)
        }
        event_creator_image.loadImage(event.creator.imageUrl)
        event_title.text = event.eventName
        event_day.text = Util.getDay(event.startTime)
        event_month.text = Util.getShortMonth(event.startTime)
        event_address.text = place.address

        share()
    }

    private fun share() {
        val domain = if(Util.devFlavor()) {
            DEBUG_DYNAMIC_LINK
        } else {
            PROD_DYNAMIC_URL
        }
        val builder = Uri.Builder()
                .scheme("https")
                .authority(CAMPUSBASH_LINK)
                .path("/")
                .appendQueryParameter("eventId", event.eventId)
        val url = builder.build()
        Log.d(TAG, url.toString())
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(url)
                .setDynamicLinkDomain(domain)
                // Open links with this app on Android
                .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                .setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(event.eventName)
                        .setDescription(event.description)
                        .setImageUrl(Uri.parse(event.placeholderImage?.url ?: ""))
                        .build())
                .buildDynamicLink()
        val dynamicLinkUri = dynamicLink.uri
        var finalUrl = dynamicLinkUri.toString()
        finalUrl = Util.fixLink(finalUrl)
        if(BuildConfig.DEBUG) {
            finalUrl += "&d=1"
        }
        Log.d(TAG, finalUrl)

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(finalUrl))
                .buildShortDynamicLink()
                .addOnCompleteListener {task ->
                    val result = task.result
                    if(task.isSuccessful && result != null) {
                        val shortLink = result.shortLink
                        val shortUrl = shortLink.toString()
                        //val flowchartLink = task.result.previewLink
                        Log.d(TAG, shortUrl)
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shortUrl)
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
                    } else {
                        Log.d(TAG, "An error occurred getting the shortLink\n${task.exception?.message}")
                        toast(R.string.error_occurred)
                    }
                }
    }

    companion object {
        private const val DEBUG_DYNAMIC_LINK = "m88p6.app.goo.gl"
        private const val PROD_DYNAMIC_URL = "hx87a.app.goo.gl"
        private const val CAMPUSBASH_LINK = "campusbash-e0ca8.firebaseapp.com"
    }
}
