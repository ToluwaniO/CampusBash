package toluog.campusbash.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by oguns on 2/22/2018.
 */
class MyBootReceiver: BroadcastReceiver() {
    private val TAG = MyBootReceiver::class.java.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Receiver called")
        if (context != null) {
            Log.d(TAG, "Scheduling job")
            Util.scheduleEventDeleteJob(context)
        }
    }

}