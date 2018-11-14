package toluog.campusbash.model.dashboard

import android.os.Parcelable
import androidx.annotation.Keep
import com.bignerdranch.expandablerecyclerview.model.Parent
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserTicket(var buyerName: String = "", var buyerEmail: String = "", var quantity: Long = 0,
                      var quantities: ArrayList<TicketQuantity> = arrayListOf(),
                      @Exclude var ticketPurchaseId: String = "", @Exclude var totalPrice: Double = 0.0): Parcelable,
        Parent<TicketQuantity> {
    override fun getChildList() = quantities

    override fun isInitiallyExpanded() = false
}

@Parcelize
data class TicketQuantity(var name: String = "", var quantity: Long = 0L): Parcelable