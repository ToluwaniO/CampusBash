package toluog.campusbash.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by oguns on 12/2/2017.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class Ticket(var name: String = "", var description: String = "", var type: String = "paid",
                  var quantity: Int = 0, var price: Double = 0.0, var currency: String = "CAD$",
                  var salesStartTime: Long = Calendar.getInstance().timeInMillis,
                  var salesEndTime: Long = Calendar.getInstance().timeInMillis,
                  var timeZone: String = Calendar.getInstance().timeZone.displayName,
                  var minAllowedPerOrder: Int = 1, var maxAllowedPerOrder: Int = quantity) : Parcelable