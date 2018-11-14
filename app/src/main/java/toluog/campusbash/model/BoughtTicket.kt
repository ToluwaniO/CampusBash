package toluog.campusbash.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class BoughtTicket(var ticketId: String = "", var buyerId: String = "", var eventId: String = "", var eventName: String = "",
                        var eventTime: Long = 0L, var placeholderImage: Media = Media(), var currency: String = "", var quantity: Int = 0,
                        var ticketCodes: List<TicketMetaData> = emptyList(), var timeSpent: Long = 0L): Parcelable