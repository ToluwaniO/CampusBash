package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModel
import android.net.Uri
import com.google.android.gms.location.places.Place
import toluog.campusbash.data.Repository
import toluog.campusbash.model.Currency
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/13/2017.
 */
class CreateEventViewModel(app: Application) : AndroidViewModel(app){

    private val repo = Repository(app.applicationContext)
    var event = Event()
    var imageUri: Uri? = null
    var place: Place? = null
    var ticket: Ticket? = null

    var selectedTicket: Ticket? = null

    fun select(ticket: Ticket) {
        selectedTicket = ticket
    }

    fun deleteTicket(index: Int) {
        event.tickets.removeAt(index)
    }

    fun addTicket(ticket: Ticket) {
        event.tickets.add(ticket)
    }

    fun getCurrencies() = repo.getCurrencies()

    fun getUniversities(country: String) = repo.getUnis(country)

}