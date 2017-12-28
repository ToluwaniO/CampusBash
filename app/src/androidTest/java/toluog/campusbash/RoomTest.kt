package toluog.campusbash

import android.arch.lifecycle.Observer
import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.matcher.ViewMatchers.assertThat
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import toluog.campusbash.TestContract.Companion.events
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.EventDao
import toluog.campusbash.data.UniversityDao

/**
 * Created by oguns on 12/28/2017.
 */
@RunWith(AndroidJUnit4::class)
class RoomTest{
    private var db: AppDatabase? = null
    private var eventDao: EventDao? = null
    private var uniDao: UniversityDao? = null
    private lateinit var context: Context

    @Before
    fun createDb(){
        context = InstrumentationRegistry.getTargetContext()
        db = AppDatabase.getDbInstance(context)
        eventDao = db?.eventDao()
        uniDao = db?.universityDao()
    }

    @After
    fun closeDb(){
        eventDao?.nukeTable()
        uniDao?.nukeTable()
        AppDatabase.destroyDbInstance()
    }

    //TABLE: EVENTS

    @Test
    fun addEvents(){
        eventDao?.insertEvent(TestContract.events[0])
        eventDao?.insertEvents(TestContract.events)
        val events = eventDao?.getEvents()
        assert(events?.value?.size == 3)
    }

    @Test
    fun updateEvents(){
        eventDao?.insertEvents(TestContract.events)
        val event = TestContract.events[1]
        event.eventName = "Manchester Derby"
        eventDao?.updateEvent(event)
        val ev = eventDao?.getEvent("2")
        assert(ev?.value?.eventName == event.eventName)
    }

    @Test
    fun deleteEvents(){
        eventDao?.insertEvents(TestContract.events)
        eventDao?.deleteEvent(TestContract.events[0])
        val events = eventDao?.getEvents()
        assert(events?.value?.size == 2)
    }

    //TABLE: UNIVERSITIES

    @Test
    fun addUniversity(){
        uniDao?.insertUniversity(TestContract.unis[0])
        uniDao?.insertUniversities(TestContract.unis)
        val unis = uniDao?.getUniversities("")
        assert(unis?.value?.size == 3)
    }

    @Test
    fun updateUniversities(){
        uniDao?.insertUniversities(TestContract.unis)
        val uni = TestContract.unis[1]
        uni.name = "University of Ottawa"
        uniDao?.updateUniversity(uni)
        val un = uniDao?.getUniversity("2")
        assert(un?.value?.name == uni.name)
    }

    @Test
    fun deleteUniversities(){
        uniDao?.insertUniversities(TestContract.unis)
        uniDao?.deleteUniversity(TestContract.unis[0])
        val unis = uniDao?.getUniversities()
        assert(unis?.value?.size == 2)
    }
}