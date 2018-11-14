package toluog.campusbash.data

import androidx.room.TypeConverter
import java.lang.reflect.Type;
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/4/2017.
 */
class EventConverter{

    @TypeConverter
    fun getTicketsString(tickets: ArrayList<Ticket>): String{
        tickets.forEach {
            it.type = it.type.toLowerCase()
        }
        val gson = Gson()
        return gson.toJson(tickets)
    }

    @TypeConverter
    fun getTicketsList(tickets: String):ArrayList<Ticket>{
        val listType = object : TypeToken<ArrayList<Ticket>>() {}.type
        val gson = Gson()
        return gson.fromJson(tickets, listType)
    }
}