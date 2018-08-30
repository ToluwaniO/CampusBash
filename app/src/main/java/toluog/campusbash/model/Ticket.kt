package toluog.campusbash.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.Keep
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by oguns on 12/2/2017.
 */
@Keep
@SuppressLint("ParcelCreator")
@Parcelize
data class Ticket(var name: String = "", var description: String = "", var type: String = "paid",
                  var quantity: Long = 0, var price: Double = 0.0, var currency: String = "CA$",
                  var salesStartTime: Long = Calendar.getInstance().timeInMillis,
                  var salesEndTime: Long = Calendar.getInstance().timeInMillis,
                  var timeZone: String = Calendar.getInstance().timeZone.displayName,
                  var minAllowedPerOrder: Int = 1, var maxAllowedPerOrder: Int = 10,
                  var ticketsSold: Long = 0, var isVisible: Boolean = true) : Parcelable