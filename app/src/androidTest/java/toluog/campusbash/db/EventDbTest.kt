package toluog.campusbash.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.runner.AndroidJUnit4
import org.junit.runner.RunWith
import toluog.campusbash.data.EventDao
import androidx.test.InstrumentationRegistry
import org.junit.Before
import toluog.campusbash.data.AppDatabase
import androidx.room.Room
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

    private val MY_UID = "z8237b23729x272937b"
    private val TYPE = "party!!"
    private val UNIVERSITY = "University of Ottawa"
    private val events = arrayListOf(
            Event().apply {
                eventId = "1"
                eventName = "1"
                eventType = "party"
                startTime = now
                endTime = now - 5000
                creator.apply {
                    uid = "qey38"
                }
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
                university = UNIVERSITY
                eventType = TYPE
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
                creator.apply {
                    uid = MY_UID
                }
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
    fun testInsert() {
        eventDao.insertEvent(events[0])
        val dbEvents = eventDao.getEvents().data()
        assertEquals(dbEvents?.get(0), events[0])
        Log.d(TAG, "testing done")
    }

    @Test
    fun testUpdate() {
        eventDao.insertEvent(events[0])
        eventDao.updateEvent(events[0].apply {
            eventName = "1000"
        })
        assertEquals(eventDao.getEvent(events[0].eventId).data()?.eventName, "1000")
    }

    @Test
    fun testGet() {
        eventDao.insertEvents(events)
        assertEquals(eventDao.getEvents(now).data()?.size, 4)
        assertEquals(eventDao.getMyEvents(MY_UID).data()?.size, 1)
        assertEquals(eventDao.getMyEvents(MY_UID).data()?.first(), events.last())

        //WITH TIME
        assertFalse(eventDao.getEvents(now).data()?.contains(events[0]) ?: false)
        assertTrue(eventDao.getEvents(now).data()?.contains(events[1]) ?: false)
        assertEquals(eventDao.getEvents(UNIVERSITY, now).data()?.size, 1)

        //SEARCH QUERY
        assertEquals(eventDao.getEventsWithQuery("4", now).data()?.first(), events[3])
        assertEquals(eventDao.getEventsWithQueryAndType("3", TYPE, now).data()?.first(), events[2])
    }

    @Test
    fun testRemove() {
        eventDao.insertEvents(events)
        assertThat(eventDao.getEvents().data()?.size, `is`(events.size))
        eventDao.nukeTable()
        assertThat(eventDao.getEvents().data()?.size, `is`(0))
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        Log.d(TAG, "@After")
        eventDao.nukeTable()
        db.close()
    }

}