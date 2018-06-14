package toluog.campusbash.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BoughtTicket(var ticketId: String = "", var buyerId: String = "", var eventId: String = "", var eventName: String = "",
                        var eventTime: Long = 0L, var placeholderImage: Media = Media(), var currency: String = "", var quantity: Int = 0,
                        var ticketCodes: List<TicketMetaData> = emptyList()): Parcelable