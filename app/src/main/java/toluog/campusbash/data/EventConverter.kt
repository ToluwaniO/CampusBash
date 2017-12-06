package toluog.campusbash.data

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/4/2017.
 */
class EventConverter{

    @TypeConverter
    fun getTicketsString(tickets: Array<Ticket>): String{
        val gson = Gson()
        return gson.toJson(tickets)
    }

    @TypeConverter
    fun getTicketsList(tickets: String):Array<Ticket>{
        val gson = Gson()
        return gson.fromJson(tickets, Array<Ticket>::class.java)
    }
}