package toluog.campusbash.model

data class TicketMetaData(var code: String = "", var isUsed: Boolean = false, var qrUrl: String,
                          var ticketName: String = "")