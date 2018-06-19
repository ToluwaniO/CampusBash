package toluog.campusbash.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View.VISIBLE
import android.arch.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import toluog.campusbash.R
import android.content.Intent
import android.util.Log
import android.widget.ArrayAdapter
import org.jetbrains.anko.intentFor
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event
import com.firebase.ui.auth.ResultCodes
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import org.jetbrains.anko.act
import toluog.campusbash.ViewBehavior.BottomNavigationBehavior
import toluog.campusbash.ViewBehavior.FabScrollBehavior
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract.Companion.RC_SIGN_IN
import kotlin.collections.ArrayList
import android.support.design.widget.AppBarLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.View.GONE
import org.jetbrains.anko.doAsync
import toluog.campusbash.utils.*
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import toluog.campusbash.adapters.BoughtTicketAdapter
import toluog.campusbash.model.BoughtTicket


class MainActivity : AppCompatActivity(), EventAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener,
                                                            BoughtTicketAdapter.TicketListener {

    private val TAG = MainActivity::class.java.simpleName
    private val fragManager = supportFragmentManager
    private lateinit var uniAdapter: ArrayAdapter<CharSequence>
    private lateinit var viewModel: MainActivityViewModel
    private val uniChar = ArrayList<CharSequence>()
    private val uniList = ArrayList<University>()
    private val firebaseManager = lazy { FirebaseManager() }
    private lateinit var country: String
    private lateinit var university: String
    private var title = ""

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_events -> {
                title = ""
                fab.visibility = VISIBLE
                fragManager.popBackStack()
                fragManager.beginTransaction().replace(R.id.fragment_frame, EventsFragment(), null)
                        .commit()
                setAppBarState(true)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                title = ""
                fab.visibility = GONE
                fragManager.beginTransaction().replace(R.id.fragment_frame, SearchEventFragment(), null)
                        .commit()
                setAppBarState(null)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tickets -> {
                title = getString(R.string.tickets)
                fab.visibility = GONE
                fragManager.popBackStack()
                fragManager.beginTransaction().replace(R.id.fragment_frame, TicketsFragment.newInstance(), null)
                        .commit()
                setAppBarState(false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_host -> {
                title = getString(R.string.my_events)
                fab.visibility = VISIBLE
                val bundle = Bundle()
                bundle.putBoolean(AppContract.MY_EVENT_BUNDLE, true)
                val fragment = EventsFragment()
                fragment.arguments = bundle
                fragManager.popBackStack()
                fragManager.beginTransaction().replace(R.id.fragment_frame, fragment, null)
                        .commit()
                setAppBarState(false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                title = getString(R.string.profile)
                fab.visibility = GONE
                fragManager.beginTransaction().replace(R.id.fragment_frame, ProfileFragment(), null)
                        .commit()
                setAppBarState(false)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Analytics.init(applicationContext)
        Fabric.with(this, Crashlytics())
        Util.cancelAllJobs(this)
        firstOpen()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        (fab.layoutParams as CoordinatorLayout.LayoutParams).behavior = FabScrollBehavior()
        //(navigation.layoutParams as CoordinatorLayout.LayoutParams).behavior = BottomNavigationBehavior()
        fab.setOnClickListener {
            startActivity(intentFor<CreateEventActivity>())
        }

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        country = Util.getPrefString(act, AppContract.PREF_COUNTRY_KEY)
        university = Util.getPrefString(act, AppContract.PREF_UNIVERSITY_KEY)

        updateUi()
        fragManager.beginTransaction().replace(R.id.fragment_frame, EventsFragment(), null).commit()
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseManager.isSignedIn()){
            Log.d(TAG, "user is signed in")
            CampusBash.init(applicationContext)
            doAsync { firebaseManager.value.updateFcmToken(applicationContext) }
        } else{
            Util.startSignInActivity(act)
        }
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

    override fun onItemClick(event: Event, view: View) {
        Analytics.logEventSelected(event)
        val bundle = Bundle()
        bundle.putString(AppContract.MY_EVENT_BUNDLE, event.eventId)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val transitionName = getString(R.string.event_card_transition)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, transitionName)

            ActivityCompat.startActivity(this, intentFor<ViewEventActivity>().putExtras(bundle),
                    options.toBundle())
        } else {
            startActivity(intentFor<ViewEventActivity>().putExtras(bundle))
        }
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
    }

    private fun firstOpen(){
        val fOpen = Util.getPrefInt(act, AppContract.PREF_FIRST_OPEN_KEY)
        Log.d(TAG, "FOPEN KEY: ${Util.getPrefInt(act, AppContract.PREF_FIRST_OPEN_KEY)}")
        if(fOpen == 0){
            Log.d(TAG, "First Open")
            startActivity(intentFor<OnBoardingActivity>())
            finish()
        } else{
            Log.d(TAG, "Not first open")
            val interestSet = Util.getPrefStringSet(act, AppContract.PREF_EVENT_TYPES_KEY)
            val university = Util.getPrefString(act, AppContract.PREF_UNIVERSITY_KEY)
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
        main_uni_spinner.setSelection(uniPosition)
    }

    private  fun setAppBarState(enabled: Boolean?) {
        val params = fragment_frame.layoutParams as CoordinatorLayout.LayoutParams
        when {
            enabled == null -> {
                appbar.setExpanded(false, false)
                appbar.visibility = View.GONE
                params.behavior = null
            }
            enabled -> {
                appbar.setExpanded(true, true)
                appbar.visibility = View.VISIBLE
                params.behavior = AppBarLayout.ScrollingViewBehavior()
            }
            else -> {
                appbar.setExpanded(false, true)
                appbar.visibility = View.VISIBLE
                params.behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
        fragment_frame.requestLayout()
        toolbar.title = title
    }
}
