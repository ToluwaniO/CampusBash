package toluog.campusbash.Utils

import android.text.TextUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import toluog.campusbash.model.Event
import com.google.firebase.firestore.FirebaseFirestore



/**
 * Created by oguns on 12/13/2017.
 */
class FirebaseManager(){
    init {
        db = FirebaseFirestore.getInstance()
    }

    companion object {
        var db: FirebaseFirestore? = null

        fun addEvent(event: Event) {
            val eventRef = db?.collection(AppContract.FIREBASE_EVENTS)
            if(TextUtils.isEmpty(event.eventId)){
                eventRef?.document()
                event.eventId = eventRef?.id ?: ""
                eventRef?.add(event)
            } else{
                db?.collection(AppContract.FIREBASE_EVENTS)?.document(event.eventId)?.set(event)
            }
            db?.collection(AppContract.FIREBASE_EVENTS)?.add(event)
        }
    }
}