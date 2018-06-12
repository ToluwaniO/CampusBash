package toluog.campusbash.model

import com.google.firebase.firestore.PropertyName

data class BoughtTicket(var ticketId: String = "", var buyerId: String = "", var eventId: String = "", var eventName: String = "",
                        var eventTime: Long = 0L, var currency: String = "", var quantity: Int = 0,
                        var ticketCodes: List<TicketMetaData> = emptyList())