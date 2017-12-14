package toluog.campusbash.model

/**
 * Created by oguns on 12/2/2017.
 */
data class Ticket(var name: String, var description: String?, var type: Int, var quantity: Int, var price: Double,
                  var salesStarts: Int, var salesStartTime: Long, var salesEnds: Int, var salesEndTime: Long)