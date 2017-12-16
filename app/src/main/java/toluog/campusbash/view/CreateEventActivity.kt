package toluog.campusbash.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract

class CreateEventActivity : AppCompatActivity(), CreateEventFragment.SaveComplete {

    private val TAG = MainActivity::class.java.simpleName
    private val fragManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        fragManager.beginTransaction().replace(R.id.fragment_frame, CreateEventFragment(), AppContract.CREATE_EVENT_TAG)
                .commit()
    }

    override fun eventSaved() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val frags = supportFragmentManager.fragments

        for (i in frags){
            if(i is CreateEventFragment){
                i.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
