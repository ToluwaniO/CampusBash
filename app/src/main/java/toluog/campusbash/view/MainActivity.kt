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
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        fab.setOnClickListener {
            startActivity(intentFor<CreateEventActivity>())
        }
        fab.visibility = GONE

        fragManager.beginTransaction().replace(R.id.fragment_frame, EventsFragment(), null).commit()

        val info: PackageInfo
        try {
            info = packageManager.getPackageInfo("toluog.campusbash", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val something = String(Base64.encode(md.digest(), 0))
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something)
            }
        } catch (e1: PackageManager.NameNotFoundException) {
            Log.e("name not found", e1.toString())
        } catch (e: NoSuchAlgorithmException) {
            Log.e("no such an algorithm", e.toString())
        } catch (e: Exception) {
            Log.e("exception", e.toString())
        }

//        val providers = Arrays.asList(
//                AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
//                AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build())
//        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
//                .setAvailableProviders(providers).build(), RC_SIGN_IN)


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
        bundle.putParcelable(AppContract.MY_EVENT_BUNDLE, event)
        startActivity(intentFor<ViewEventActivity>().putExtras(bundle))
    }
}
