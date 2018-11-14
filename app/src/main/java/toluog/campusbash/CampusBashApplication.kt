package toluog.campusbash

import android.app.Application
import android.graphics.Color
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.core.provider.FontRequest
import android.util.Log
import com.crashlytics.android.Crashlytics
import toluog.campusbash.utils.Analytics

class CampusBashApplication: Application() {
    private val TAG = CampusBashApplication::class.java.simpleName
    override fun onCreate() {
        super.onCreate()

        val config: EmojiCompat.Config
        val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)
        config = FontRequestEmojiCompatConfig(applicationContext, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Log.d(TAG, "EmojiCompat initialized")
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Log.e(TAG, "EmojiCompat initialization failed", throwable)
                        throwable?.let { Crashlytics.logException(throwable) }
                    }
                })
        EmojiCompat.init(config)
        Analytics.init(applicationContext)
    }
}