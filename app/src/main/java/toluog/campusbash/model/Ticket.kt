package toluog.campusbash.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by oguns on 12/2/2017.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class Ticket(var name: String = "", var description: String? = null, var type: Int = 0, var quantity: Int = 0, var price: Double = 0.0,
                  var salesStarts: Int = 0, var salesStartTime: Long = 0L, var salesEnds: Int = 0, var salesEndTime: Long = 0L) : Parcelable