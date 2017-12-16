package toluog.campusbash.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by oguns on 12/2/2017.
 */
data class Creator(var name: String = "", var imageUrl: String = "", var uid: String = "") : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(imageUrl)
        writeString(uid)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Creator> = object : Parcelable.Creator<Creator> {
            override fun createFromParcel(source: Parcel): Creator = Creator(source)
            override fun newArray(size: Int): Array<Creator?> = arrayOfNulls(size)
        }
    }
}