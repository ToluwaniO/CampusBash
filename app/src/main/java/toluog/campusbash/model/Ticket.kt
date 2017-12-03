package toluog.campusbash.model

/**
 * Created by oguns on 12/2/2017.
 */
data class Ticket(var name: String, var description: String?, var type: Int, var quanttity: Int, var price: Double,
                  var salesStarts: Int, var salesStartTime: String, var salesEnds: Int, var salesEndTime: String)