package toluog.campusbash.model

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 12/2/2017.
 */
@Entity(tableName = "Events")
data class Event(@PrimaryKey var eventId: String, var eventName: String, var eventType: String, var description: String, var placeholderUrl: String?,
                 var eventVideoUrl: String?, var university: String, var locationAddress: String, @Embedded var latLng: LatLng,
                 var startTime: Long, var endTime: Long, var notes: String?, var tickets: ArrayList<Ticket>, @Embedded var creator: Creator) : Parcelable {
    constructor() : this("", "", "", "", null, null,
            "", "", LatLng(0.0, 0.0), 0L, 0L, null,
            arrayListOf(Ticket("", null, 0, 0, 0.0, 0,
                    0L, 0, 0L)), AppContract.CREATOR)

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable<LatLng>(LatLng::class.java.classLoader),
            source.readLong(),
            source.readLong(),
            source.readString(),
            source.createTypedArrayList(Ticket.CREATOR),
            source.readParcelable<Creator>(Creator::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(eventId)
        writeString(eventName)
        writeString(eventType)
        writeString(description)
        writeString(placeholderUrl)
        writeString(eventVideoUrl)
        writeString(university)
        writeString(locationAddress)
        writeParcelable(latLng, 0)
        writeLong(startTime)
        writeLong(endTime)
        writeString(notes)
        writeTypedList(tickets)
        writeParcelable(creator, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(source: Parcel): Event = Event(source)
            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }
    }
}