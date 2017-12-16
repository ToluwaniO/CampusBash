package toluog.campusbash.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by oguns on 12/2/2017.
 */
data class Ticket(var name: String = "", var description: String? = null, var type: Int = 0, var quantity: Int = 0, var price: Double = 0.0,
                  var salesStarts: Int = 0, var salesStartTime: Long = 0L, var salesEnds: Int = 0, var salesEndTime: Long = 0L) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readDouble(),
            source.readInt(),
            source.readLong(),
            source.readInt(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(description)
        writeInt(type)
        writeInt(quantity)
        writeDouble(price)
        writeInt(salesStarts)
        writeLong(salesStartTime)
        writeInt(salesEnds)
        writeLong(salesEndTime)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Ticket> = object : Parcelable.Creator<Ticket> {
            override fun createFromParcel(source: Parcel): Ticket = Ticket(source)
            override fun newArray(size: Int): Array<Ticket?> = arrayOfNulls(size)
        }
    }
}