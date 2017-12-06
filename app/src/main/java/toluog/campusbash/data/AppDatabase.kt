package toluog.campusbash.data

import android.arch.persistence.room.*
import android.content.Context
import toluog.campusbash.model.Event

/**
 * Created by oguns on 12/4/2017.
 */
@Database(entities = arrayOf(Event::class), version = 1)
@TypeConverters(EventConverter::class)
abstract class AppDatabase(): RoomDatabase() {

    abstract fun eventDao() : EventDao

    companion object {
        var dbInstance: AppDatabase? = null
        val dbName = "CampusBashDB"

        fun getDbInstance(context: Context): AppDatabase?{
            if(dbInstance == null){
                dbInstance = Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()
            }
            return dbInstance
        }

        fun destroyDbInstance(){
            dbInstance = null
        }
    }

}