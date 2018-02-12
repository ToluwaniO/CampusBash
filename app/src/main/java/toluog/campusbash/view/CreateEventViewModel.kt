package toluog.campusbash.view

import android.arch.lifecycle.ViewModel
import android.net.Uri
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/13/2017.
 */
class CreateEventViewModel : ViewModel(){

    var event = Event()
    var imageUri: Uri? = null

}