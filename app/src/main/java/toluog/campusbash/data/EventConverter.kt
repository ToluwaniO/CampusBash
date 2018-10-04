package toluog.campusbash.data

import android.arch.persistence.room.TypeConverter
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
        return Gson().toJson(tickets)
    }

    @TypeConverter
    fun getTicketsList(tickets: String):ArrayList<Ticket>{
        val listType = object : TypeToken<ArrayList<Ticket>>() {}.type
        return Gson().fromJson(tickets, listType)
    }

    @TypeConverter
    fun getUniversitiesString(universities: ArrayList<String>) = Gson().toJson(universities)

    @TypeConverter
    fun getUniversitiesList(universities: String): ArrayList<String> {
        val listType = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(universities, listType)
    }
}