package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 12/28/2017.
 */
@Dao
public interface UniversityDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUniversity(university: University)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUniversities(universities: List<University>)

    @Update
    fun updateUniversity(university: University)

    @Update
    fun updateUniversities(universities: List<University>)

    @Query("SELECT * FROM ${AppContract.UNIVERSITY_TABLE} WHERE uniId LIKE :id LIMIT 1")
    fun getUniversity(id: String): LiveData<University>

    @Query("SELECT * FROM ${AppContract.UNIVERSITY_TABLE} WHERE country LIKE :country")
    fun getUniversities(country: String): LiveData<List<University>>

    @Query("SELECT * FROM ${AppContract.UNIVERSITY_TABLE} WHERE name LIKE :query")
    fun queryUniversities(query: String): LiveData<List<University>>

    @Query("SELECT * FROM ${AppContract.UNIVERSITY_TABLE}")
    fun getUniversities(): LiveData<List<University>>

    @Delete
    fun deleteUniversity(university: University)

    @Delete
    fun deleteUniversities(universities: List<University>)

    @Query("DELETE FROM ${AppContract.UNIVERSITY_TABLE}")
    fun nukeTable()
}