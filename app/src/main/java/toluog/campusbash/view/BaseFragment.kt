package toluog.campusbash.view

import android.support.v4.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class BaseFragment: Fragment(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}