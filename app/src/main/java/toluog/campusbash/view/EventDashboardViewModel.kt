package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.LiveData
import com.google.android.gms.tasks.Task
import toluog.campusbash.model.Event
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.view.GeneralViewModel

class EventDashboardViewModel(app: Application): GeneralViewModel(app) {

    private var event: LiveData<Event>? = null

    fun getEvent(eventId: String): LiveData<Event>? {
        if(event == null) {
            event = repo.getEvent(eventId)
        }
        return event
    }

    fun getUsersWithTickets(eventId: String) = repo.getUserWithTickets(eventId)

    fun getTicketMetadatas(eventId: String) = repo.getTicketMetaDatas(eventId)

    fun updateTicket(fbManager: FirebaseManager, eventId: String, ticketId: String, key: String,
                     value: Any, code: String): Task<Task<Void>>? {
        return fbManager.updateTicketField(eventId, ticketId, key, value, code)
    }
}