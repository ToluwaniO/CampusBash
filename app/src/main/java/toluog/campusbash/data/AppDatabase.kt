package toluog.campusbash.data

import android.arch.persistence.room.*
import android.content.Context
import toluog.campusbash.model.Currency
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.model.University

/**
 * Created by oguns on 12/4/2017.
 */
@Database(entities = arrayOf(Event::class, University::class, Currency::class, Place::class), version = 1)
@TypeConverters(EventConverter::class)
abstract class AppDatabase(): RoomDatabase() {

    abstract fun eventDao() : EventDao
    abstract fun universityDao(): UniversityDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun placeDao(): PlaceDao

    companion object {
        private var dbInstance: AppDatabase? = null
        private val dbName = "CampusBashDB"

        fun getDbInstance(context: Context): AppDatabase?{
            if(dbInstance == null){
                synchronized(this) {
                    if (dbInstance == null) {
                        dbInstance = Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()
                    }
                }
            }
            return dbInstance
        }

        fun destroyDbInstance(){
            dbInstance = null
        }
    }

}