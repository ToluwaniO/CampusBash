package toluog.campusbash.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TicketPriceBreakdown(var ticketFee: Int = 0, var serviceFee: Int = 0, var paymentFee: Int = 0,
                                var campusbashFee: Int = 0, var totalFee: Int = 0): Parcelable

fun TicketPriceBreakdown.toMap(): Map<String, Int> {
    return mapOf(
            "ticketFee" to ticketFee,
            "serviceFee" to serviceFee,
            "paymentFee" to paymentFee,
            "campusbashFee" to campusbashFee,
            "totalFee" to totalFee
    )
}