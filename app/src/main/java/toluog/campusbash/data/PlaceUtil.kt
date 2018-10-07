package toluog.campusbash.data

import android.content.Context
import android.util.Log
import toluog.campusbash.data.datasource.GeneralDataSource
import java.util.concurrent.TimeUnit

class PlaceUtil {
    companion object {
        private val TAG = PlaceUtil::class.java.simpleName
        suspend fun savePlace(placeId: String, context: Context) {
            val db = AppDatabase.getDbInstance(context)
            val place = db?.placeDao()?.getStaticPlace(placeId)
            val fetchPlace = if(place == null) {
                true
            } else {
                System.currentTimeMillis() - place.timeSaved >= TimeUnit.DAYS.toMillis(30)
            }

            if(fetchPlace) {
                GeneralDataSource.fetchPlace(placeId, context)
            } else {
                Log.d(TAG, "Place already saved")
            }
        }
    }
}