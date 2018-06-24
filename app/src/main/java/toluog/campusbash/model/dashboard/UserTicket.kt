package toluog.campusbash.model.dashboard

import android.os.Parcelable
import com.bignerdranch.expandablerecyclerview.model.Parent
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserTicket(var buyerName: String = "", var buyerEmail: String = "", var quantity: Long = 0,
                      var quantities: ArrayList<TicketQuantity> = arrayListOf()): Parcelable,
        Parent<TicketQuantity> {
    override fun getChildList() = quantities

    override fun isInitiallyExpanded() = false
}

@Parcelize
data class TicketQuantity(var name: String = "", var quantity: Long = 0L): Parcelable