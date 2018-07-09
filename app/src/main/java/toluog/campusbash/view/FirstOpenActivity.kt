package toluog.campusbash.view

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.act
import org.jetbrains.anko.intentFor
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import com.firebase.ui.auth.ErrorCodes.NO_NETWORK
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_first_open.*
import org.jetbrains.anko.design.snackbar
import toluog.campusbash.utils.AppContract.Companion.RC_SIGN_IN


class FirstOpenActivity : AppCompatActivity(), PickUniversityFragment.PickUniversityListener,
PickEventTypeFragment.PickEventTypeListener, NoNetworkFragment.OnFragmentInteractionListener{

    private val fragManager = supportFragmentManager
    private lateinit var viewModel: GeneralViewModel
    private val fbManager = FirebaseManager()
    private var user: FirebaseUser? = null
    private var count = 0
    private val TAG = FirstOpenActivity::class.java.simpleName

    private var country: String? = null
    private var university: String? = null
    private var prefs: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_open)

        Log.d(TAG, "Activity opened")
        user = FirebaseManager.getUser()
        viewModel = ViewModelProviders.of(this).get(GeneralViewModel::class.java)
    }

    override fun universitySelectionDone(country: String, name: String) {
        Util.setPrefString(act, AppContract.PREF_UNIVERSITY_KEY, name)
        Util.setPrefString(act, AppContract.PREF_COUNTRY_KEY, country)
        user?.let {
            fbManager.updateProfileField(AppContract.FIREBASE_USER_UNIVERSITY, name, it)
            fbManager.updateProfileField(AppContract.FIREBASE_USER_COUNTRY, country, it)
        }
        if(prefs == null) {
            count++
            fragManager.beginTransaction().replace(R.id.fragment_frame, PickEventTypeFragment(), null).commit()
        } else {
            Util.setPrefInt(act, AppContract.PREF_FIRST_OPEN_KEY, 1)
            startActivity(intentFor<SetupProfileActivity>())
            finish()
        }
    }

    override fun eventsPickDone(selected: Set<String>) {
        Util.setPrefStringSet(act, AppContract.PREF_EVENT_TYPES_KEY, selected)
        user?.let { fbManager.updateProfileField(AppContract.FIREBASE_USER_PREFERENCES, selected.toList(), it) }
        Log.d(TAG, "EventSet $selected")
        Util.setPrefInt(act, AppContract.PREF_FIRST_OPEN_KEY, 1)
        startActivity(intentFor<SetupProfileActivity>())
        finish()
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseManager.isSignedIn()){
            Log.d(TAG, "user is signed in")
            user = FirebaseManager.getUser()
            observeUser()
        } else{
            Util.startSignInActivity(act)
        }
        if(!Util.isConnected(this)) {
            fragManager.beginTransaction().replace(R.id.fragment_frame, NoNetworkFragment(),
                    null).commit()
        } else if(country == null || university == null) {
            count++
            fragManager.beginTransaction().replace(R.id.fragment_frame, PickUniversityFragment(), null).commit()
        } else if (prefs == null) {
            count++
            fragManager.beginTransaction().replace(R.id.fragment_frame, PickEventTypeFragment(), null).commit()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                Util.downloadCurrencies(this)
            } else {
               Log.d(TAG, "Sign in failed")
            }
        }
    }

    private fun observeUser() {
        user?.let {
            viewModel.getProfileInfo(it)?.observe(this, Observer {
                country = it?.get(AppContract.FIREBASE_USER_COUNTRY) as String?
                university = it?.get(AppContract.FIREBASE_USER_UNIVERSITY) as String?
                prefs = it?.get(AppContract.FIREBASE_USER_PREFERENCES) as List<String>?
            })
        }
    }

    override fun onTryAgainClicked(connected: Boolean) {
        if(connected) {
            startActivity(intentFor<FirstOpenActivity>())
            finish()
        } else {
            snackbar(container, R.string.no_internet)
        }
    }
}
