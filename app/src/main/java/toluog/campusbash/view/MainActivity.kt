package toluog.campusbash.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.arch.lifecycle.*
import android.arch.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import toluog.campusbash.R
import android.content.Intent
import android.util.Log
import android.widget.ArrayAdapter
import org.jetbrains.anko.intentFor
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.utils.AppContract
import com.firebase.ui.auth.ResultCodes
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import com.firebase.ui.auth.IdpResponse
import org.jetbrains.anko.act
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract.Companion.RC_SIGN_IN
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), EventAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private val TAG = MainActivity::class.java.simpleName
    private val fragManager = supportFragmentManager
    private lateinit var uniAdapter: ArrayAdapter<CharSequence>
    private lateinit var viewModel: MainActivityViewModel
    private val uniChar = ArrayList<CharSequence>()
    private val uniList = ArrayList<University>()
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

        firstOpen()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        fab.setOnClickListener {
            startActivity(intentFor<CreateEventActivity>())
        }
        fab.visibility = GONE

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        updateUi()
        fragManager.beginTransaction().replace(R.id.fragment_frame, EventsFragment(), null).commit()
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseManager.isSignedIn()){
            Log.d(TAG, "user is signed in")
        } else{
            Util.startSignInActivity(act)
        }
    }

    private fun updateUi() {
        uniAdapter = ArrayAdapter(this, R.layout.text_view_layout, uniChar)
        viewModel.getUniversities()?.observe(this, Observer { unis ->
            if(unis != null && uniList.isEmpty()){
                for (i in unis){
                    uniList.add(i)
                    uniChar.add(i.shortName)
                }
                uniAdapter.notifyDataSetChanged()
            }
        })
        main_uni_spinner.adapter = uniAdapter
        main_uni_spinner.onItemSelectedListener = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")

        if (requestCode == RC_SIGN_IN) {

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

    override fun onItemClick(event: Event) {
        val bundle = Bundle()
        bundle.putString(AppContract.MY_EVENT_BUNDLE, event.eventId)
        startActivity(intentFor<ViewEventActivity>().putExtras(bundle))
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        (view as TextView?)?.setTextColor(Color.WHITE)
    }

    private fun firstOpen(){
        val fOpen = Util.getPrefInt(act, AppContract.PREF_FIRST_OPEN_KEY)
        if(fOpen == 0){
            Util.setPrefInt(act, AppContract.PREF_FIRST_OPEN_KEY, 1)
            startActivity(intentFor<FirstOpenActivity>())
            finish()
        }
    }
}
