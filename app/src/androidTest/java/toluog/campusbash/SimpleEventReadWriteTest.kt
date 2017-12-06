package toluog.campusbash

import android.app.Instrumentation
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase.*;
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import toluog.campusbash.data.*
import toluog.campusbash.model.Creator
import toluog.campusbash.model.Event
import toluog.campusbash.model.LatLng
import toluog.campusbash.model.Ticket
import java.util.*

/**
 * Created by oguns on 12/4/2017.
 */
@RunWith(AndroidJUnit4::class)
class SimpleEventReadWriteTest{
    var db: AppDatabase? = null
    var eventDao: EventDao? = null

    @Before
    fun createDb(){
        val context = InstrumentationRegistry.getTargetContext();
        db = AppDatabase.getDbInstance(context)
        eventDao = db?.eventDao()
    }

    @After
    fun closeDb(){
        AppDatabase.destroyDbInstance()
    }

    @Test
    fun writeAndRead(){
        val ticket = Ticket("hjjk", "hj", 0, 0, 15.50, 0,
                "hj", 0, "hj")
        val creator = Creator("hkj", "hj", "gghj")
        var event = Event("hfgjh", "ghjhbkj", "ghjhk", "gjhbkj", null,
                "cgfhgvj", "gjhk", "gvhj", LatLng(.0,.0),
                "jb", "hbj", "dhfjk", arrayOf(ticket), creator)
        eventDao?.insertEvent(event)
        val ticketTwo = eventDao?.getEvents()
        val observable = ticketTwo?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({events ->
                    assertEquals(events[0].description, "gjhbkj")
                })

    }

}