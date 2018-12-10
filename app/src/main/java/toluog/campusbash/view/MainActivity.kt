package toluog.campusbash.view

import android.app.Activity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.view.View.VISIBLE
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import toluog.campusbash.R
import android.content.Intent
import android.util.Log
import android.widget.ArrayAdapter
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event
import androidx.lifecycle.ViewModelProviders
import android.graphics.Color
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import toluog.campusbash.ViewBehavior.FabScrollBehavior
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract.Companion.RC_SIGN_IN
import kotlin.collections.ArrayList
import com.google.android.material.appbar.AppBarLayout
import androidx.core.content.ContextCompat
import android.view.View.GONE
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import toluog.campusbash.utils.*
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import toluog.campusbash.adapters.BoughtTicketAdapter
import toluog.campusbash.model.BoughtTicket
import toluog.campusbash.utils.extension.intentFor
import toluog.campusbash.view.viewmodel.MainActivityViewModel


class MainActivity : AppCompatActivity(), EventAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener,
                                                            BoughtTicketAdapter.TicketListener {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var uniAdapter: ArrayAdapter<CharSequence>
    private lateinit var viewModel: MainActivityViewModel
    private val uniChar = ArrayList<CharSequence>()
    private val uniList = ArrayList<University>()
    private val firebaseManager = lazy { FirebaseManager() }
    private lateinit var country: String
    private var university: String = ""
    private var firstDropSelect = false
    private lateinit var navController: NavController

    private val threadJob = Dispatchers.Default
    private val threadScope = CoroutineScope(threadJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.main_nav_host) as NavHostFragment? ?: return

        navController = host.navController
        navigation.setupWithNavController(navController)
        
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            setAppBarState(destination.id)
        }

        Analytics.init(applicationContext)
        Fabric.with(this, Crashlytics())
        Util.cancelAllJobs(this)
        firstOpen()
        Log.d(TAG, "init started")

        setSupportActionBar(toolbar)

        (fab.layoutParams as CoordinatorLayout.LayoutParams).behavior = FabScrollBehavior()
        fab.setOnClickListener {
            startActivity(intentFor<CreateEventActivity>())
        }

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        Log.d(TAG, "Viewmodel init")
        country = Util.getPrefString(this, AppContract.PREF_COUNTRY_KEY)
        university = Util.getPrefString(this, AppContract.PREF_UNIVERSITY_KEY)
        Log.d(TAG, "Pref string gotten")

        updateUi()
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseManager.isSignedIn()){
            Log.d(TAG, "user is signed in")
            CampusBash.init(applicationContext)
            threadScope.launch { firebaseManager.value.updateFcmToken(applicationContext) }
        } else{
            Util.startSignInActivity(this)
        }
    }

    override fun onDestroy() {
        threadJob.cancel()
        super.onDestroy()
    }

    private fun updateUi() {
        uniAdapter = ArrayAdapter(this, R.layout.text_view_layout, uniChar)
        viewModel.getUniversities(country)?.observe(this, Observer { unis ->
            if(unis != null && uniList.isEmpty()){
                for (i in unis){
                    uniList.add(i)
                    uniChar.add(i.shortName)
                }
                uniAdapter.notifyDataSetChanged()
                updateUniversityAdapter()
            }
        })
        main_uni_spinner.adapter = uniAdapter
        main_uni_spinner.onItemSelectedListener = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")

        if (requestCode == RC_SIGN_IN) {

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                Log.d(TAG, "Sign in successful")
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
                Log.d(TAG, "Sign in failed")
            }
        }

        supportFragmentManager.fragments.forEach {
            if(it is ProfileFragment) {
                it.onActivityResult(requestCode, resultCode, data)
            }
        }


    }

    override fun onItemClick(event: Event, view: View) {
        Analytics.logEventSelected(event)
        val options = navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_right
                popExit = R.anim.slide_out_left
            }
        }
        navController.navigate(R.id.navigation_view_event, bundleOf(AppContract.EVENT_ID to event.eventId), options)
    }

    override fun onTicketClicked(ticket: BoughtTicket) {
        startActivity(intentFor<ViewBoughtTicketActivity>().putExtras(Bundle().apply {
            putParcelable(AppContract.BOUGHT_TICKET, ticket)
        }))
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        (view as TextView?)?.setTextColor(Color.WHITE)
        if(!firstDropSelect) {
            university = uniList[position].name
            navController.navigate(R.id.navigation_events, bundleOf(
                    "university" to university,
                    "myEvents" to 0)
            )
        } else {
            firstDropSelect = false
        }
    }

    private fun firstOpen(){
        val fOpen = Util.getPrefInt(this, AppContract.PREF_FIRST_OPEN_KEY)
        Log.d(TAG, "FOPEN KEY: ${Util.getPrefInt(this, AppContract.PREF_FIRST_OPEN_KEY)}")
        if(fOpen == 0){
            Log.d(TAG, "First Open")
            startActivity(intentFor<OnBoardingActivity>())
            finish()
        } else{
            Log.d(TAG, "Not first open")
            val interestSet = Util.getPrefStringSet(this, AppContract.PREF_EVENT_TYPES_KEY)
            val university = Util.getPrefString(this, AppContract.PREF_UNIVERSITY_KEY)
            Log.d(TAG, "INTERESTS $interestSet")
            Log.d(TAG, "UNIVERSITY $university")
        }
    }

    private fun updateUniversityAdapter() {
        var uniPosition = 0
        for (i in 0 until uniList.size) {
            if(uniList[i].name == university) {
                uniPosition = i
                break
            }
        }
        firstDropSelect = true
        main_uni_spinner.setSelection(uniPosition)
    }

    private  fun setAppBarState(id: Int) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        when(id) {
            R.id.navigation_search -> {
                appbar.setExpanded(false, true)
                toolbar.title = getString(R.string.find_events)
                (fab as View).visibility = GONE
            }
            R.id.navigation_events -> {
                appbar.setExpanded(true, true)
                if (navigation.selectedItemId == R.id.navigation_host) {
                    toolbar.title = getString(R.string.my_events)
                    (fab as View).visibility = VISIBLE
                } else {
                    toolbar.title = ""
                }
            }
            R.id.navigation_host -> {
                appbar.setExpanded(false, true)
                toolbar.title = getString(R.string.host)
                (fab as View).visibility = VISIBLE
            }
            R.id.navigation_tickets -> {
                appbar.setExpanded(false, true)
                toolbar.title = getString(R.string.tickets)
                (fab as View).visibility = GONE
            }
            R.id.navigation_profile -> {
                appbar.setExpanded(false, true)
                toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.background_material_light))
                toolbar.title = ""
                (fab as View).visibility = VISIBLE
            }
        }
    }
}
