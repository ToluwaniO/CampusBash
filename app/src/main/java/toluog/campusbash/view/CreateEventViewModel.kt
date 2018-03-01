package toluog.campusbash.view

import android.arch.lifecycle.ViewModel
import android.net.Uri
import com.google.android.gms.location.places.Place
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/13/2017.
 */
class CreateEventViewModel : ViewModel(){

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

}