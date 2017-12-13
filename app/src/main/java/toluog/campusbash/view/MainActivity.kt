package toluog.campusbash.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import toluog.campusbash.R

import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.EventDao


class MainActivity : AppCompatActivity() {

    var db: AppDatabase? = null
    var eventDao: EventDao? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_events -> {
                //message.setText(R.string.title_events)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_host -> {
                //message.setText(R.string.title_host)
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
