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
import toluog.campusbash.BuildConfig
import toluog.campusbash.R

/**
 * Created by oguns on 1/27/2018.
 */
class AdManager(val context: Context, val adList: MutableLiveData<ArrayList<NativeAd>>) {
    private val admobAppId: String
    private val TAG = AdManager::class.java.simpleName

    init {
        if (BuildConfig.DEBUG) {
            admobAppId = context.getString(R.string.admob_app_id_debug)
        } else {
            admobAppId = context.getString(R.string.admob_app_id)
        }
    }

    fun loadAds() {
        val ads = adList.value
        if(ads != null) {
            if (ads.size >= AppContract.NUM_ADS) return

            val builder = AdLoader.Builder(context, admobAppId)

            val adLoader = builder.forAppInstallAd { ad: NativeAppInstallAd ->
                Log.d(TAG, "LOADING APP INSTALL AD ${ad.toString()}")
                ads.add(ad)
                adList.postValue(ads)
                loadAds()
            }
            .forContentAd { ad: NativeContentAd ->
                Log.d(TAG, "LOADING CONTENT AD ${ad.toString()}")
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
        fun initializeAds(context: Context) {
            MobileAds.initialize(context, context.getString(R.string.admob_app_id))
        }
    }

}