package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import toluog.campusbash.model.Place
import toluog.campusbash.utils.AppContract

@Dao
public interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlace(place: Place)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaces(places: List<Place>)

    @Update
    fun updatePlaces(places: List<Place>)

    @Update
    fun updatePlace(place: Place)

    @Query("SELECT * FROM $TABLE WHERE $COLUMN_ID LIKE :id LIMIT 1")
    fun getStaticPlace(id: String): Place?

    @Query("SELECT * FROM $TABLE WHERE $COLUMN_ID LIKE :id LIMIT 1")
    fun getPlace(id: String): LiveData<Place>

    @Query("Select * FROM $TABLE")
    fun getPlaces(): LiveData<List<Place>>

    @Query("Select * FROM $TABLE")
    fun getStaticPlaces(): List<Place>?

    @Delete()
    fun deletePlace(place: Place)

    @Query("DELETE FROM $TABLE WHERE $COLUMN_ID LIKE :id")
    fun deletePlace(id: String)

    @Query("DELETE FROM $TABLE")
    fun nukeTable()

    companion object {
        private const val TABLE = AppContract.PLACE_TABLE
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_LAT_LNG = "latLng"
    }

}