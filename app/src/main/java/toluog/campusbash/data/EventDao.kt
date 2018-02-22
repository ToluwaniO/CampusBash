package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.*
import toluog.campusbash.model.Event
import toluog.campusbash.utils.AppContract
import java.util.*

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

    @Query("SELECT * FROM ${AppContract.EVENT_TABLE} WHERE eventId LIKE :id LIMIT 1")
    fun getEvent(id: String): LiveData<Event>

    @Query("Select * FROM ${AppContract.EVENT_TABLE}")
    fun getEvents():LiveData<List<Event>>

    @Query("SELECT * FROM ${AppContract.EVENT_TABLE} WHERE endTime > :date")
    fun getEvents(date: Long): LiveData<List<Event>>

    @Delete()
    fun deleteEvent(event: Event)

    @Query("DELETE FROM ${AppContract.EVENT_TABLE} WHERE endTime < :date")
    fun deleteEvents(date: Long)

    @Query("DELETE FROM ${AppContract.EVENT_TABLE}")
    fun nukeTable()
}