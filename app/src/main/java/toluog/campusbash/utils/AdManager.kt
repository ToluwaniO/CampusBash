package toluog.campusbash.utils

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide.init
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAd
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.NativeAppInstallAd
import com.google.android.gms.ads.formats.NativeContentAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import toluog.campusbash.BuildConfig
import toluog.campusbash.R

/**
 * Created by oguns on 1/27/2018.
 */
class AdManager(val context: Context, val adList: MutableLiveData<ArrayList<NativeAd>>) {

    fun loadAds() {
        if(!isInitialized) {
            initializeAds(context)
        }

        val ads = adList.value
        if(ads != null) {
            if (ads.size >= configProvider.eventsFragmentAdsMax()) return

            val builder = AdLoader.Builder(context, admobAppId)

            val adLoader = builder.forAppInstallAd { ad: NativeAppInstallAd ->
                Log.d(TAG, "LOADING APP INSTALL AD")
                ads.add(ad)
                adList.postValue(ads)
                loadAds()
            }
            .forContentAd { ad: NativeContentAd ->
                Log.d(TAG, "LOADING CONTENT AD")
                ads.add(ad)
                adList.postValue(ads)
                loadAds()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    Log.e(TAG, "Ad failed to load with error code $errorCode")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build())
            .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    companion object {
        private val TAG = AdManager::class.java.simpleName
        private var admobAppId: String = ""
        private var isInitialized = false
        private val configProvider = ConfigProvider(FirebaseRemoteConfig.getInstance())

        fun initializeAds(context: Context) {
            Log.d(TAG, "Initializing MobileAds")
            if (BuildConfig.DEBUG) {
                admobAppId = context.getString(R.string.admob_app_id_debug)
            } else {
                admobAppId = context.getString(R.string.admob_app_id)
            }
            MobileAds.initialize(context, admobAppId)
            isInitialized = true
            Log.d(TAG, "MobileAds initialized")
        }
    }

}