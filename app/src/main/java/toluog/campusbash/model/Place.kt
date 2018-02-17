package toluog.campusbash.model

import android.annotation.SuppressLint
import android.arch.persistence.room.Embedded
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by oguns on 2/11/2018.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class Place(var id: String = "", var name: String = "", var address: String = "",
                 @Embedded var latLng: LatLng = LatLng()) : Parcelable