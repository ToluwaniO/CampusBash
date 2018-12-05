package toluog.campusbash.data

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.*
import androidx.room.migration.Migration
import android.content.Context
import com.crashlytics.android.Crashlytics
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
                        dbInstance = try {
                            Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                                .addMigrations(MIGRATION_1_2)
                                .build()
                        } catch (e: IllegalStateException) {
                            Crashlytics.logException(e)
                            Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                                    .fallbackToDestructiveMigration()
                                    .build()
                        }
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
                        " ${AppContract.UNIVERSITIES} TEXT NOT NULL DEFAULT '[]'")
                database.execSQL("CREATE TABLE  ${AppContract.EVENT_TABLE}_backup (eventId TEXT NOT NULL," +
                        " eventName TEXT NOT NULL, eventType TEXT NOT NULL, description TEXT NOT NULL," +
                        " university TEXT NOT NULL, startTime INTEGER NOT NULL, endTime INTEGER NOT NULL," +
                        " timeZone TEXT NOT NULL, placeId TEXT NOT NULL, ticketsSold INTEGER NOT NULL," +
                        " address TEXT NOT NULL, universities TEXT NOT NULL, placeholderImage_url TEXT," +
                        " placeholderImage_path TEXT, placeholderImage_type TEXT, eventVideo_url TEXT," +
                        " eventVideo_path TEXT, eventVideo_type TEXT, name TEXT NOT NULL, imageUrl " +
                        "TEXT NOT NULL, uid TEXT NOT NULL, stripeAccountId TEXT, PRIMARY KEY(eventId))")
                database.execSQL("DROP TABLE ${AppContract.EVENT_TABLE}")
                database.execSQL("ALTER TABLE ${AppContract.EVENT_TABLE}_backup RENAME TO ${AppContract.EVENT_TABLE}")
            }

        }
    }

}