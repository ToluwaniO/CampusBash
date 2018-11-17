package toluog.campusbash.data

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.Context
import toluog.campusbash.model.Currency
import toluog.campusbash.model.Event
import toluog.campusbash.model.Place
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 12/4/2017.
 */
@Database(entities = arrayOf(Event::class, University::class, Currency::class, Place::class), version = 2)
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
                        dbInstance = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                                .addMigrations(MIGRATION_1_2)
                                .build()
                    }
                }
            }
            return dbInstance
        }

        fun destroyDbInstance(){
            dbInstance = null
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ${AppContract.EVENT_TABLE} ADD COLUMN " +
                        " ${AppContract.UNIVERSITIES} TEXT NOT NULL")
            }

        }
    }

}