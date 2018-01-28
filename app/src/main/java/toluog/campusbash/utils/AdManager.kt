package toluog.campusbash.utils

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide.init
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAd
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.NativeAppInstallAd
import com.google.android.gms.ads.formats.NativeContentAd
import toluog.campusbash.R

/**
 * Created by oguns on 1/27/2018.
 */
class AdManager(val context: Context, val adList: ArrayList<NativeAd>) {
    val admobAppId: String
    val TAG = AdManager::class.java.simpleName

    init {
        admobAppId = context.getString(R.string.admob_app_id)
    }

    fun initializeAds() {
        MobileAds.initialize(context, admobAppId)
    }

    fun loadAds() {
        val adLoader = AdLoader.Builder(context, admobAppId)
                .forAppInstallAd { ad : NativeAppInstallAd ->
                    adList.add(ad)
                }
                .forContentAd { ad : NativeContentAd ->
                    adList.add(ad)
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
    }

}