package toluog.campusbash.model

import android.annotation.SuppressLint
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 2/11/2018.
 */
@Keep
@SuppressLint("ParcelCreator")
@Entity(tableName = AppContract.PLACE_TABLE)
@Parcelize
data class Place(@PrimaryKey var id: String = "", var name: String = "", var address: String = "",
                 @Embedded var latLng: LatLng = LatLng(), var timeSaved: Long = System.currentTimeMillis()) : Parcelable