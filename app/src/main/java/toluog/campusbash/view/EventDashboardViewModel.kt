package toluog.campusbash.view

import android.app.Application
import toluog.campusbash.view.GeneralViewModel

class EventDashboardViewModel(app: Application): GeneralViewModel(app) {

    fun getUsersWithTickets(eventId: String) = repo.getUserWithTickets(eventId)

    fun getTicketMetadatas(eventId: String) = repo.getTicketMetaDatas(eventId)
}