package toluog.campusbash.view.viewmodel

import android.app.Application
import android.net.Uri
import toluog.campusbash.data.datasource.GeneralDataSource
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/13/2017.
 */
class CreateEventViewModel(app: Application) : GeneralViewModel(app){

    var event = Event()
    var imageUri: Uri? = null
    var place: Place? = null
    var ticket: Ticket? = null

    var selectedTicket: Ticket? = null

    init {
        val now = System.currentTimeMillis()
        event.startTime = now
        event.endTime = now
    }

    fun select(ticket: Ticket) {
        selectedTicket = ticket
    }

    fun deleteTicket(index: Int) {
        event.tickets.removeAt(index)
    }

    fun addTicket(ticket: Ticket) {
        event.tickets.add(ticket)
    }

    fun getCurrencies() = generalRepository.getCurrencies()

    fun getUniversities(country: String) = generalRepository.getUniversities(country)

    fun getUser(uid: String) = GeneralDataSource.getUser(uid)

    fun getPlace(id: String) = generalRepository.getPlace(id)

    fun savePlace() {
        val p = place
        if(p != null) generalRepository.savePlace(p)
    }

}