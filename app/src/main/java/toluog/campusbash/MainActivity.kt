package toluog.campusbash

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.EventDao
import toluog.campusbash.model.Creator
import toluog.campusbash.model.Event
import toluog.campusbash.model.LatLng
import toluog.campusbash.model.Ticket


class MainActivity : AppCompatActivity() {

    var db: AppDatabase? = null
    var eventDao: EventDao? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_events -> {
                message.setText(R.string.title_events)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_host -> {
                message.setText(R.string.title_host)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


    }
}
