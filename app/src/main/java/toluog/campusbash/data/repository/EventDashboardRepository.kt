package toluog.campusbash.data.repository

import android.content.Context
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.datasource.EventDashboardDataSource
import kotlin.coroutines.CoroutineContext

class EventDashboardRepository (eventId: String, override val coroutineContext: CoroutineContext): Repository() {
    private val dataSource = EventDashboardDataSource(coroutineContext)

    init {
        dataSource.initListener(eventId)
    }

    fun getEvent(eventId: String, context: Context) = AppDatabase.getDbInstance(context)?.eventDao()?.getEvent(eventId)

    fun getUserWithTickets() = dataSource.getTickets()

    fun getTicketMetaDatas() = dataSource.getMetadatas()

    override fun clear() {
        dataSource.clear()
    }

}