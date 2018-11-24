package toluog.campusbash.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import androidx.annotation.NonNull
import android.util.Log
import com.bumptech.glide.Glide.init
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


/**
 * Created by oguns on 2/6/2018.
 */
class ConfigProvider(private val remoteConfig: FirebaseRemoteConfig) {

    private val TAG = ConfigProvider::class.java.simpleName
    private val defaults = mapOf(
            FeatureKey.MIN_EVENTS_FOR_ADS to 20,
            FeatureKey.EVENTS_FRAGMENT_ADS_ENABLED to false,
            FeatureKey.EVENTS_FRAGMENT_ADS_MAX to 5,
            FeatureKey.CAMPUSBASH_TICKET_CUT to 1,
            FeatureKey.STRIPE_TICKET_CUT to 2.9,
            FeatureKey.STRIPE_SERVICE_FEE to 0.30,
            FeatureKey.CAMPUSBASH_SERVICE_FEE to 0.10,
            FeatureKey.FEATURED_EVENTS_TYPES to ""
    )

    init {
        remoteConfig.setDefaults(defaults)
        fetch()
    }

    fun isAdsEventsFragmentEnabled() = remoteConfig.getBoolean(FeatureKey.EVENTS_FRAGMENT_ADS_ENABLED)

    fun eventsFragmentAdsMax() = remoteConfig.getString(FeatureKey.EVENTS_FRAGMENT_ADS_MAX).toInt()

    fun minEventsToDisplayAds() = remoteConfig.getString(FeatureKey.MIN_EVENTS_FOR_ADS).toInt()

    fun campusbashTicketCut() = remoteConfig.getDouble(FeatureKey.CAMPUSBASH_TICKET_CUT)

    fun campusbashServiceFee() = remoteConfig.getDouble(FeatureKey.CAMPUSBASH_SERVICE_FEE)

    fun stripeTicketCut() = remoteConfig.getDouble(FeatureKey.STRIPE_TICKET_CUT)

    fun stripeServiceFee() = remoteConfig.getDouble(FeatureKey.STRIPE_SERVICE_FEE)

    fun featuredEventTypes(): Set<String> {
        val raw = remoteConfig.getString(FeatureKey.FEATURED_EVENTS_TYPES)
        if(raw.isNullOrBlank()) {
            return emptySet()
        }
        val list = raw.split(",").toMutableList()
        for(i in 0 until list.size) {
            list[i] = list[i].trim()
        }
        return list.toHashSet()
    }

    private fun fetch() {
        Log.d(TAG, "FETCHING CONFIG VALUES")
        remoteConfig.fetch(AppContract.configRefreshTime)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        remoteConfig.activateFetched()
                        Log.d(TAG, "Remote Config values fetched")
                    } else {
                        Log.d(TAG, "Remote Config values fetch failed")
                    }
                }
    }

}