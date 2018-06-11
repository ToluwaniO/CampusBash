package toluog.campusbash.view

import android.app.Application
import android.content.Context
import com.squareup.leakcanary.RefWatcher
import com.squareup.leakcanary.LeakCanary



class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        refWatcher = LeakCanary.install(this)
        // Normal app init code...
    }

    private var refWatcher: RefWatcher? = null

    companion object {
        fun getRefWatcher(context: Context?): RefWatcher? {
            val application = context?.applicationContext as MainApplication?
            return application?.refWatcher
        }
    }
}