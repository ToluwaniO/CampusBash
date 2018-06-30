package toluog.campusbash.model

import android.os.Parcelable
import android.support.annotation.Keep
import android.util.ArrayMap
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TicketMetaData(var code: String = "", var isUsed: Boolean = false, var qrUrl: String = "",
                          var ticketName: String = "", @Exclude var ticketPurchaseId: String = ""): Parcelable {
    fun toMap() = ArrayMap<String, Any>().apply {
        put("code", code)
        put("isUsed", isUsed)
        put("qrUrl", qrUrl)
        put("ticketName", ticketName)
    }
}