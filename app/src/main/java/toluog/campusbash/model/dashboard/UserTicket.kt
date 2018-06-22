package toluog.campusbash.model.dashboard

data class UserTicket(var buyerName: String = "", var buyerEmail: String = "", var quantity: Int = 0,
                      var quantities: ArrayList<TicketQuantity> = arrayListOf())

data class TicketQuantity(var name: String = "", var quantity: Int = 0)