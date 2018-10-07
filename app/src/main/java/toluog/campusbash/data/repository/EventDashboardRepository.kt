package toluog.campusbash.data.repository

import android.content.Context
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.datasource.EventDashboardDataSource

class EventDashboardRepository (eventId: String): Repository {
    private val dataSource = EventDashboardDataSource()

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