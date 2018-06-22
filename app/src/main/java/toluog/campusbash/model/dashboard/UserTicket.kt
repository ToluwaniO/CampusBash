package toluog.campusbash.model.dashboard

import android.os.Parcelable
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserTicket(var buyerName: String = "", var buyerEmail: String = "", var quantity: Int = 0,
                      var quantities: ArrayList<TicketQuantity> = arrayListOf()): Parcelable,
        ExpandableGroup<TicketQuantity>(buyerEmail, quantities)

@Parcelize
data class TicketQuantity(var name: String = "", var quantity: Int = 0): Parcelable