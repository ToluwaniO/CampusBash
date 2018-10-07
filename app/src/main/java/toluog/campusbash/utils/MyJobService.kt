package toluog.campusbash.utils

import android.util.Log
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import kotlinx.coroutines.launch
import toluog.campusbash.data.AppDatabase

/**
 * Created by oguns on 2/22/2018.
 */
class MyJobService: JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Starting job")
        val tag = params?.tag
        when(tag) {
            AppContract.JOB_EVENT_DELETE -> deleteOldEvents()
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job stopped!")
        return false
    }

    private fun deleteOldEvents() {
        Log.d(TAG, "Deleting old events")
        val db = AppDatabase.getDbInstance(this)
        launch { db?.eventDao()?.deleteEvents(System.currentTimeMillis()) }
        Log.d(TAG, "Done deleting old events")
    }

    companion object {
        private val TAG = MyJobService::class.java.simpleName
    }

}