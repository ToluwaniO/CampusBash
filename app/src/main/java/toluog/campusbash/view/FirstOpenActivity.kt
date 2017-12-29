package toluog.campusbash.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.act
import org.jetbrains.anko.intentFor
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

class FirstOpenActivity : AppCompatActivity(), PickUniversityFragment.PickUniversityListener,
PickEventTypeFragment.PickEventTypeListener{

    val fragManager = supportFragmentManager
    var count = 0
    private val TAG = FirstOpenActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_open)
        Log.d(TAG, "Activity opened")

    }

    override fun universitySelectionDone(name: String) {
        launch { Util.setPrefString(act, AppContract.PREF_UNIVERSITY_KEY, name) }
        count++
        fragManager.beginTransaction().replace(R.id.fragment_frame, PickEventTypeFragment(), null).commit()
    }

    override fun eventsPickDone(selected: Set<String>) {
        Util.setPrefStringSet(act, AppContract.PREF_EVENT_TYPES_KEY, selected)
        startActivity(intentFor<MainActivity>())
        finish()
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseManager.isSignedIn()){
            Log.d(TAG, "user is signed in")
        } else{
            Util.startSignInActivity(act)
        }
        count++
        fragManager.beginTransaction().add(R.id.fragment_frame, PickUniversityFragment(), null).commit()
    }

    override fun onBackPressed() {
        Log.d(TAG, "$count")

        if(count == 0 || count == 1){
            super.onBackPressed()
            if(count == 1) count--
        } else {
            fragManager.popBackStack()
            fragManager.beginTransaction().replace(R.id.fragment_frame, PickUniversityFragment(), null).commit()
            count--
        }
    }
}
