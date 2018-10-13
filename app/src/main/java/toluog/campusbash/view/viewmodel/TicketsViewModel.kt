package toluog.campusbash.view.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.view.viewmodel.GeneralViewModel


class TicketsViewModel(app: Application) : GeneralViewModel(app) {
    private var tickets: LiveData<List<BoughtTicket>>? = null

    fun getTickets(): LiveData<List<BoughtTicket>>? {
        if(tickets == null) {
            tickets = generalRepository.getTickets()
        }
        return tickets
    }

}
