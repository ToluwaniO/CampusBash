package toluog.campusbash.utils

import android.content.Context
import android.util.Log
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.datasource.GeneralDataSource
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import java.util.concurrent.TimeUnit

class DbManager {
    companion object {

        private val TAG = DbManager::class.java.simpleName

        suspend fun deleteInvalidPlaces(context: Context) {
            val db = AppDatabase.getDbInstance(context)
            if(db != null) {
                val places = db.placeDao().getStaticPlaces()
                var events = db.eventDao().getStaticEvents()
                events = events?.sortedWith(compareBy { it.placeId })

                places?.forEach {
                    val event = placeIsUsed(it.id, events ?: emptyList())
                    if(!placeIsValid(it) && event != null) {
                        Log.d(TAG, "RE-FETCHING PLACE -> $it")
                        GeneralDataSource.fetchPlace(it.id, context)
                    } else if(!placeIsValid(it) || event == null) {
                        Log.d(TAG, "DELETING PLACE -> $it")
                        db.placeDao().deletePlace(it.id)
                    } else {
                        Log.d(TAG, "place cache is valid")
                    }
                }
            }
        }

        private fun placeIsValid(place: Place): Boolean {
            val time = place.timeSaved
            return System.currentTimeMillis() - time < TimeUnit.DAYS.toMillis(29)
        }

        private fun placeIsUsed(id: String, events: List<Event>): Event? {
            val index = events.map { it.placeId }.indexOf(id)
            if(index >= 0) return events[index]
            return null
        }
    }
}