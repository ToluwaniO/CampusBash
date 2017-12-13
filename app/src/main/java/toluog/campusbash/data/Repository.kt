package toluog.campusbash.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.model.Event

/**
 * Created by oguns on 12/7/2017.
 */
class Repository(c: Context){
    val eventDataSource = EventDataSource()
    val mFireStore = FirebaseFirestore.getInstance()
    val context = c
    fun getEvents(){
        EventDataSource.initListener(context)
    }

    fun addEvent(event: Event){
        val collectionRef = mFireStore.collection(AppContract.FIREBASE_EVENTS)
        collectionRef.add(event)
    }

}