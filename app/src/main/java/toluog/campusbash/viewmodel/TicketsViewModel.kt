package toluog.campusbash.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import toluog.campusbash.model.BoughtTicket


class TicketsViewModel(app: Application) : GeneralViewModel(app) {
    private var tickets: LiveData<List<BoughtTicket>>? = null

    fun getTickets(): LiveData<List<BoughtTicket>>? {
        if(tickets == null) {
            tickets = repo.getTickets()
        }
        return tickets
    }

}
