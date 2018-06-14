package toluog.campusbash.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TicketMetaData(var code: String = "", var isUsed: Boolean = false, var qrUrl: String = "",
                          var ticketName: String = ""): Parcelable