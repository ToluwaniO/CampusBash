package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.android.synthetic.main.event_card_layout.*
import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.loadImage
import toluog.campusbash.view.viewmodel.ViewEventViewModel
import android.content.pm.ResolveInfo
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_share_event.*
import kotlinx.android.synthetic.main.share_action_view.*
import android.content.ComponentName
import android.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import toluog.campusbash.utils.FirebaseManager


class ShareEventActivity : AppCompatActivity() {

    private val TAG = ShareEventActivity::class.java.simpleName
    private lateinit var event: Event
    private lateinit var viewModel: ViewEventViewModel
    private val shareActions = ArrayList<ResolveInfo>()
    private var shortUrl = ""

    private val threadJob = Dispatchers.Default
    private val threadScope = CoroutineScope(threadJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_event)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.share_event)
        viewModel = ViewModelProviders.of(this).get(ViewEventViewModel::class.java)

        event = intent.extras?.getParcelable(AppContract.MY_EVENT_BUNDLE) ?: Event()

        action_recycler.apply {
            adapter = ShareAdapter()
            layoutManager = LinearLayoutManager(this@ShareEventActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        viewModel.getPlace(event.placeId)?.observe(this, Observer {
            if (it != null) {
                updateUi(it)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id) {
            android.R.id.home -> onBackPressed()
        }
        return true
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
                .setDomainUriPrefix(domain)
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
        if(Util.debugMode()) {
            finalUrl += "&d=1"
        }
        Log.d(TAG, finalUrl)

        threadScope.launch {
            val shortLink = FirebaseManager.getShortDynamicLink(finalUrl)
            if (shortLink != null) {
                shortUrl = shortLink.shortLink.toString()
                //val flowchartLink = task.result.previewLink
                Log.d(TAG, shortUrl)
            } else {
                shortUrl = finalUrl
            }

            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shortUrl)
                getShareApps(shareIntent)
                share_progress.visibility = View.GONE
                action_recycler.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun getShareApps(sendIntent: Intent) {
        val resolveInfoList = packageManager
                .queryIntentActivities(sendIntent, 0)
        for (info in resolveInfoList) {
            val pkg = info.activityInfo.packageName.toLowerCase()
            if (isPriorityApp(pkg)) {
                shareActions.add(0, info)
            } else {
                shareActions.add(info)
            }
        }
    }

    private fun isPriorityApp(packageName: String): Boolean {
        for (app in priorityAppNames) {
            if (packageName.toLowerCase().contains(app)) return true
        }
        return false
    }

    inner class ShareAdapter: RecyclerView.Adapter<ShareAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.share_action_view, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = shareActions.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val action = shareActions[position]
            holder.bindApp(action)
        }

        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
            fun bindApp(info: ResolveInfo) {
                val resources = packageManager.getResourcesForApplication(info.activityInfo.applicationInfo.packageName)
                action_icon.setImageDrawable(resources.getDrawable(info.iconResource))
                action_label.text = info.loadLabel(packageManager)

                containerView.setOnClickListener {
                    val activity = info.activityInfo
                    val name = ComponentName(activity.applicationInfo.packageName, activity.name)
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(Intent.EXTRA_TEXT, shortUrl)
                    i.component = name
                    startActivity(i)
                }
            }
        }

    }

    companion object {
        private const val DEBUG_DYNAMIC_LINK = "https://m88p6.app.goo.gl"
        private const val PROD_DYNAMIC_URL = "https://hx87a.app.goo.gl"
        private const val CAMPUSBASH_LINK = "campusbash-e0ca8.firebaseapp.com"
        private val priorityAppNames = arrayListOf(
                "instagram",
                "snap",
                "messenger",
                "facebook"
        )
    }
}
