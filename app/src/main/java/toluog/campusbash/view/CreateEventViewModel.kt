package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.data.Repository
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

    fun getCurrencies() = repo.getCurrencies()

    fun getUniversities(country: String) = repo.getUnis(country)

    fun getUser(uid: String) = repo.getUser(uid)

    fun getPlace(id: String) = repo.getPlace(id)

    fun savePlace() {
        val p = place
        if(p != null) repo.savePlace(p)
    }

}