package toluog.campusbash.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main.*
import toluog.campusbash.R
import toluog.campusbash.data.AppContract

import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.EventDao
import android.R.array
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.create_event_layout.*


class MainActivity : AppCompatActivity() {

    var db: AppDatabase? = null
    var eventDao: EventDao? = null
    val fragManager = supportFragmentManager

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_events -> {
                //message.setText(R.string.title_events)
                fab.visibility = GONE
                val bundle = Bundle()
                bundle.putBoolean("myevent", true)
                val fragment = EventsFragment()
                fragment.arguments = bundle
                fragManager.beginTransaction().replace(R.id.fragment_frame, fragment, null)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_host -> {
                fab.visibility = VISIBLE
                fragManager.beginTransaction().replace(R.id.fragment_frame, EventsFragment(), null)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        fab.setOnClickListener {
            fragManager.beginTransaction().replace(R.id.fragment_frame, CreateEventFragment(), null).commit()
            fab.visibility = GONE
        }
        fab.visibility = GONE
        updateUi()
    }

    fun updateUi(){

    }
}
