package toluog.campusbash.model

import android.annotation.SuppressLint
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 12/2/2017.
 */
@SuppressLint("ParcelCreator")
@Entity(tableName = AppContract.EVENT_TABLE)
@Parcelize
data class Event(@PrimaryKey var eventId: String = "", var eventName: String = "", var eventType: String = "",
                 var description: String = "", var placeholderUrl: String? = null,
                 var eventVideoUrl: String? = null, var university: String = "", @Embedded var place: Place = Place(),
                 var startTime: Long = 0L, var endTime: Long = 0L, var notes: String? = null,
                 var tickets: ArrayList<Ticket> = ArrayList(),
                 @Embedded var creator: Creator = AppContract.CREATOR): Parcelable