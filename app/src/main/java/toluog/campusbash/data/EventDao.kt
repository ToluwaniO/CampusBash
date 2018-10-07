package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import toluog.campusbash.model.Event
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 12/4/2017.
 */
@Dao
public interface EventDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvents(event: List<Event>)

    @Update
    fun updateEvents(events: List<Event>)

    @Update
    fun updateEvent(event: Event)

    @Query("SELECT * FROM $TABLE WHERE eventId LIKE :id LIMIT 1")
    fun getEvent(id: String): LiveData<Event>

    @Query("Select * FROM $TABLE")
    fun getEvents():LiveData<List<Event>>

    @Query("Select * FROM $TABLE")
    fun getStaticEvents():List<Event>?

    @Query("SELECT * FROM $TABLE WHERE endTime > :date AND university LIKE :university")
    fun getEvents(university: String, date: Long): LiveData<List<Event>>

    @Query("SELECT * FROM $TABLE WHERE endTime > :date")
    fun getEvents(date: Long): LiveData<List<Event>>

    @Query("SELECT * FROM $TABLE WHERE placeId LIKE :placeId")
    fun getStaticEventsByPlace(placeId: String): List<Event>?

    @Query("SELECT * FROM $TABLE WHERE uid LIKE :uid")
    fun getMyEvents(uid: String): LiveData<List<Event>>

    @Query("SELECT * FROM $TABLE WHERE eventName LIKE :name AND startTime >= :time")
    fun getEventsWithQuery(name: String, time: Long): LiveData<List<Event>>

    @Query("SELECT * FROM $TABLE WHERE eventName LIKE :name AND eventType LIKE :type AND startTime >= :time")
    fun getEventsWithQueryAndType(name: String, type: String, time: Long): LiveData<List<Event>>

    @Delete()
    fun deleteEvent(event: Event)

    @Query("DELETE FROM $TABLE WHERE endTime < :date AND uid != :uid")
    fun deleteOldEvents(date: Long, uid: String)

    @Query("DELETE FROM $TABLE")
    fun nukeTable()

    companion object {
        private const val TABLE = AppContract.EVENT_TABLE
    }
}