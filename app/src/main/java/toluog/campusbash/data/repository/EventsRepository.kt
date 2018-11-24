package toluog.campusbash.data.repository

import androidx.lifecycle.LiveData
import android.content.Context
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.FirestorePaths
import toluog.campusbash.data.FirestoreQuery
import toluog.campusbash.data.FirestoreQueryType
import toluog.campusbash.data.datasource.EventsDataSource
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.utils.FirebaseManager
import kotlin.coroutines.CoroutineContext

class EventsRepository(val context: Context, override val coroutineContext: CoroutineContext): Repository() {
    private val TAG = EventsRepository::class.java.simpleName
    private val dataSource = EventsDataSource(context, coroutineContext)
    private val myDataSource = EventsDataSource(context, coroutineContext)
    private val db = AppDatabase.getDbInstance(context)

    fun getEvent(eventId: String) = db?.eventDao()?.getEvent(eventId)

    fun getEvents(university: String): LiveData<List<Event>>? {
        val now = System.currentTimeMillis()
        val query = hashSetOf(FirestoreQuery(FirestorePaths.UNIVERSITIES, university,
                FirestoreQueryType.ARRAY_CONTAINS), FirestoreQuery(FirestorePaths.END_TIME, now,
                FirestoreQueryType.GREATER_THAN_EQUAL_TO))
        dataSource.listenForEvents(FirestorePaths.EVENTS_PATH, query, university)
        return db?.eventDao()?.getEvents(university, now)
    }

    fun getMyEvents(): LiveData<List<Event>>? {
        val uid = FirebaseManager.getUser()?.uid ?: ""
        val query = hashSetOf(FirestoreQuery(FirestorePaths.UID, uid, FirestoreQueryType.EQUAL_TO))
        myDataSource.listenForEvents(FirestorePaths.EVENTS_PATH, query)
        return db?.eventDao()?.getMyEvents(uid)
    }

    fun getEventTickets(eventId: String) = dataSource.getTicketData(eventId)

    fun getEventsWithQueryAndType(name: String, type: String, time: Long) =
            db?.eventDao()?.getEventsWithQueryAndType(name, type, time)

    fun getEventsWithQuery(name: String, time: Long) = db?.eventDao()?.getEventsWithQuery(name, time)

    fun getPlace(id: String) = db?.placeDao()?.getPlace(id)

    fun downloadEvent(eventId: String): LiveData<Event>? {
        dataSource.downloadEvent(eventId)
        return getEvent(eventId)
    }

    override fun clear() {
        dataSource.clear()
        myDataSource.clear()
    }

}