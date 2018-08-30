package toluog.campusbash.model

import android.annotation.SuppressLint
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.Keep
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 12/2/2017.
 */
@Keep
@SuppressLint("ParcelCreator")
@Entity(tableName = AppContract.EVENT_TABLE)
@Parcelize
data class Event(@PrimaryKey var eventId: String = "", var eventName: String = "", var eventType: String = "",
                 var description: String = "", @Embedded(prefix = "placeholderImage_") var placeholderImage: Media? = null,
                 @Embedded(prefix = "eventVideo_") var eventVideo: Media? = null, var university: String = "",
                 var startTime: Long = 0L, var endTime: Long = 0L, var timeZone: String = "",
                 var placeId: String = "", var tickets: ArrayList<Ticket> = ArrayList(),
                 @Embedded var creator: Creator = Creator(), var ticketsSold: Long = 0,
                 @Exclude var address: String = ""): Parcelable {

    override fun equals(other: Any?): Boolean {
        val o = other as Event?
        if(o != null) {
            return o.eventId == eventId
        }
        return false
    }

    fun deepEquals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = eventId.hashCode()
        result = 31 * result + eventName.hashCode()
        result = 31 * result + eventType.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (placeholderImage?.hashCode() ?: 0)
        result = 31 * result + (eventVideo?.hashCode() ?: 0)
        result = 31 * result + university.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + timeZone.hashCode()
        result = 31 * result + placeId.hashCode()
        result = 31 * result + tickets.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }
}
