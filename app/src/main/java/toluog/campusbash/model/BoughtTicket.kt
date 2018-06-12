package toluog.campusbash.model

data class BoughtTicket(var ticketId: String = "", var buyerId: String = "", var eventId: String = "", var eventName: String = "",
                        var eventTime: Long = 0L, var currency: String = "", var quantity: Int = 0,
                        var tickets: List<TicketMetaData> = emptyList())