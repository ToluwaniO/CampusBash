package toluog.campusbash.model

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.firebase.firestore.GeoPoint
import toluog.campusbash.Utils.AppContract
import java.lang.reflect.Constructor

/**
 * Created by oguns on 12/2/2017.
 */
@Entity(tableName = "Events")
data class Event(@PrimaryKey var eventId: String, var eventName: String, var eventType: String, var description: String, var placeholderUrl: String?,
                 var eventVideoUrl: String?, var university: String, var locationAddress: String, @Embedded var latLng: LatLng,
                 var startTime: Long, var endTime: Long, var notes: String?, var tickets: ArrayList<Ticket>, @Embedded var creator: Creator){
    constructor():this("", "", "", "", null, null,
            "", "", LatLng(0.0,0.0), 0L, 0L, null,
            arrayListOf(Ticket("", null, 0, 0, 0.0, 0,
                    0L, 0, 0L)), AppContract.CREATOR)
}