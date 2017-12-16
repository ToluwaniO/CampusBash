package toluog.campusbash.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint

/**
 * Created by oguns on 12/2/2017.
 */
data class LatLng(var lat: Double = 0.0, var lon: Double = 0.0) : Parcelable {
    constructor(source: Parcel) : this(
            source.readDouble(),
            source.readDouble()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeDouble(lat)
        writeDouble(lon)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<LatLng> = object : Parcelable.Creator<LatLng> {
            override fun createFromParcel(source: Parcel): LatLng = LatLng(source)
            override fun newArray(size: Int): Array<LatLng?> = arrayOfNulls(size)
        }
    }
}