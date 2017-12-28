package toluog.campusbash.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main.*
import toluog.campusbash.R

import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.EventDao
import android.R.array
import android.content.Intent
import android.util.Log
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.create_event_layout.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.support.v4.intentFor
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.utils.AppContract
import android.provider.SyncStateContract.Helpers.update
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.firebase.ui.auth.ResultCodes
import android.R.attr.data
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.act
import toluog.campusbash.utils.AppContract.Companion.RC_SIGN_IN
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import java.util.*


class MainActivity : AppCompatActivity(), EventAdapter.OnItemClickListener {

    private val TAG = MainActivity::class.java.simpleName
    private val fragManager = supportFragmentManager

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_events -> {
                //message.setText(R.string.title_events)
                fab.visibility = GONE
                val bundle = Bundle()
                bundle.putBoolean("myevent", true)
                val fragment = EventsFragment()
                fragment.arguments = bundle
                fragManager.popBackStack()
                fragManager.beginTransaction().replace(R.id.fragment_frame, fragment, null)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_host -> {
                fab.visibility = VISIBLE
                fragManager.popBackStack()
                fragManager.beginTransaction().replace(R.id.fragment_frame, EventsFragment(), null)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                fragManager.beginTransaction().replace(R.id.fragment_frame, ProfileFragment(), null)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launch { firstOpen() }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        fab.setOnClickListener {
            startActivity(intentFor<CreateEventActivity>())
        }
        fab.visibility = GONE

        fragManager.beginTransaction().replace(R.id.fragment_frame, EventsFragment(), null).commit()

        updateUi()
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseManager.isSignedIn()){
            Log.d(TAG, "user is signed in")
        } else{
            Util.startSignInActivity(act)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == ResultCodes.OK) {
                // Successfully signed in
                val user = FirebaseManager.getUser()
                Log.d(TAG, "Sign in successful")
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
                Log.d(TAG, "Sign in failed")
            }
        }

    }

    fun updateUi(){

    }

    override fun onItemClick(event: Event) {
        val bundle = Bundle()
        bundle.putString(AppContract.MY_EVENT_BUNDLE, event.eventId)
        startActivity(intentFor<ViewEventActivity>().putExtras(bundle))
    }

    fun firstOpen(){
        val fOpen = Util.getPrefInt(act, AppContract.PREF_FIRST_OPEN_KEY)
        if(fOpen == 0){
            Util.setPrefInt(act, AppContract.PREF_FIRST_OPEN_KEY, 1)
        }
    }
}
