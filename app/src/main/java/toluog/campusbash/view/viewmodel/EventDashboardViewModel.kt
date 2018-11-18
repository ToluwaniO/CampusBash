package toluog.campusbash.view.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import com.google.android.gms.tasks.Task
import toluog.campusbash.data.datasource.EventsDataSource
import toluog.campusbash.data.repository.EventDashboardRepository
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.model.TicketMetaData
import toluog.campusbash.model.dashboard.UserTicket
import toluog.campusbash.utils.FirebaseManager

class EventDashboardViewModel(app: Application): GeneralViewModel(app) {

    private var repository: EventDashboardRepository? = null
    private var event: LiveData<Event>? = null

    fun getEvent(eventId: String): LiveData<Event>? {
        checkRepository(eventId)
        if(event == null) {
            event = repository?.getEvent(eventId, app.applicationContext)
        }
        return event
    }

    fun getEventTickets(eventId: String): LiveData<List<Ticket>>? {
        return repository?.getTickets(eventId, app.applicationContext)
    }

    fun getUsersWithTickets(eventId: String): LiveData<List<UserTicket>>? {
        checkRepository(eventId)
        return repository?.getUserWithTickets()
    }

    fun getTicketMetadatas(eventId: String): LiveData<Map<String, TicketMetaData>>? {
        checkRepository(eventId)
        return repository?.getTicketMetaDatas()
    }

    fun updateTicket(fbManager: FirebaseManager, eventId: String, ticketId: String, key: String,
                     value: Any, code: String): Task<Task<Void>>? {
        return fbManager.updateTicketField(eventId, ticketId, key, value, code)
    }

    private fun checkRepository(eventId: String) {
        if (repository == null) {
            repository = EventDashboardRepository(eventId, coroutineContext)
        }
    }

    override fun onCleared() {
        repository?.clear()
        super.onCleared()
    }
}