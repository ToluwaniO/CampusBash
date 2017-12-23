package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.*
import toluog.campusbash.model.Event
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

    @Query("Select * from Events")
    fun getEvents():LiveData<List<Event>>

    @Delete
    fun deleteEvent(event: Event)
}