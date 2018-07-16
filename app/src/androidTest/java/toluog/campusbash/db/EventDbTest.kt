package toluog.campusbash.db

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.support.test.runner.AndroidJUnit4
import org.junit.runner.RunWith
import toluog.campusbash.data.EventDao
import android.support.test.InstrumentationRegistry
import org.junit.Before
import toluog.campusbash.data.AppDatabase
import android.arch.persistence.room.Room
import android.util.Log
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import toluog.campusbash.data
import toluog.campusbash.model.Event
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class EventDbTest {
    private val TAG = EventDbTest::class.java.simpleName
    private val context = InstrumentationRegistry.getTargetContext()
    private val now = System.currentTimeMillis()
    private lateinit var eventDao: EventDao
    private lateinit var db: AppDatabase

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val events = arrayListOf(
            Event().apply {
                eventId = "1"
                eventName = "1"
                eventType = "party"
                startTime = now
                endTime = now - 5000
            },
            Event().apply {
                eventId = "2"
                eventName = "2"
                eventType = "party"
                startTime = now
                endTime = now + 5000
            },
            Event().apply {
                eventId = "3"
                eventName = "3"
                eventType = "party"
                startTime = now
                endTime = now + 5000
            },
            Event().apply {
                eventId = "4"
                eventName = "4"
                eventType = "party"
                startTime = now
                endTime = now + 5000
            },
            Event().apply {
                eventId = "5"
                eventName = "5"
                eventType = "party"
                startTime = now
                endTime = now + 5000
            }
    )

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        eventDao = db.eventDao()
        Log.d(TAG, "db created")
    }

    @Test
    fun testAdd() {
        eventDao.insertEvent(events[0])
        val dbEvents = eventDao.getEvents().data()
        assertEquals(dbEvents?.get(0), events[0])
        Log.d(TAG, "testing done")
    }

    @Test
    fun testRemove() {
        eventDao.insertEvents(events)
        assertThat(eventDao.getEvents().data()?.size, `is`(events.size))
        eventDao.nukeTable()
        assertThat(eventDao.getEvents().data()?.size, `is`(0))
    }

    @Test
    fun testDbWithTime() {
        eventDao.insertEvents(events)
        assertFalse(eventDao.getEvents(now).data()?.contains(events[0]) ?: false)
        assertTrue(eventDao.getEvents(now).data()?.contains(events[1]) ?: false)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        Log.d(TAG, "@After")
        eventDao.nukeTable()
        db.close()
    }

}