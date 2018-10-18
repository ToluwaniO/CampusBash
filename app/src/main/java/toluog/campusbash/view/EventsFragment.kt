package toluog.campusbash.view

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.formats.NativeAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.events_layout.*
import kotlinx.android.synthetic.main.no_events_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.UI
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Featured
import toluog.campusbash.model.Place
import toluog.campusbash.utils.ConfigProvider
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.view.viewmodel.EventsViewModel
import java.util.concurrent.Executors

/**
 * Created by oguns on 12/13/2017.
 */
class EventsFragment : Fragment(){

    private lateinit var rootView: View
    private var myEvents = false
    private var university: String = ""
    private val TAG = EventsFragment::class.java.simpleName
    private lateinit var viewModel: EventsViewModel
    private val configProvider = ConfigProvider(FirebaseRemoteConfig.getInstance())
    private var adapter: EventAdapter? = null
    private val events: ArrayList<Any> = ArrayList()
    private val ads = ArrayList<NativeAd>()
    private var eventSize = 0
    private var places: LiveData<List<Place>>? = null
    private lateinit var featured: Featured
    private lateinit var featuredTypes: Set<String>
    private val froshGroup = HashSet<String>()
    private var studentId = ""
    private val threadJob = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val threadScope = CoroutineScope(threadJob)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.events_layout, container, false)
        featuredTypes = configProvider.featuredEventTypes()
        featured = Featured(title = getString(R.string.featured_emoji, AppContract.FIRE_EMOJI))
        arguments?.let {
            myEvents = it.getBoolean(MY_EVENTS_PARAM)
            university = it.getString(UNIVERSITY_PARAM) ?: ""
            Log.d(TAG, "university=$university")
        }
        viewModel = ViewModelProviders.of(activity!!).get(EventsViewModel::class.java)
        places = viewModel.getPlaces()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setNestedScrollingEnabled(event_recycler, !myEvents)
        adapter = EventAdapter(arrayListOf(), viewModel.getPlaces()?.value ?: emptyList(),
                rootView.context, myEvents)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(rootView.context)
        event_recycler.layoutManager = layoutManager
        event_recycler.itemAnimator = DefaultItemAnimator()
        event_recycler.adapter = adapter

        observeFrosh()
        observeProfile()
        observeEvents()
        observePlaces()
        observeAds()
    }

    private fun updateUiVisibility() {
        if(eventSize == 0) {
            event_recycler.visibility = View.GONE
            no_events.visibility = View.VISIBLE
        } else {
            event_recycler.visibility = View.VISIBLE
            no_events.visibility = View.GONE
        }
    }

    private fun copyAds(eventSize: Int) {
        if(eventSize < configProvider.minEventsToDisplayAds()
                || ads.size < configProvider.eventsFragmentAdsMax()) return
        Log.d(TAG, "Copying ads main list")
        clearAds()
        val offset = events.size / ads.size + 1
        var index = 2
        Log.d(TAG, "AD NUMBER: ${ads.size}")
        for (ad in ads) {
            if(index < events.size && events[index] !is NativeAd){
                events.add(index, ad)
                index += offset
            }
            else{
                break
            }
        }
    }

    private fun clearAds() {
        (0 until events.size)
                .filter { events[it] is NativeAd }
                .forEach {
                    events.remove(it)
                }
    }

    private fun observeProfile() {
        val user = FirebaseManager.getUser() ?: return
        viewModel.getProfileInfo(user)?.observe(this, Observer {
            if (it != null) {
                studentId = it[AppContract.FIREBASE_USER_STUDENT_ID] as String? ?: ""
            }
        })
    }

    private fun observeEvents() {
        viewModel.getEvents(university, myEvents)?.observe(this, Observer { eventsList ->
            threadScope.launch {
                Log.d(TAG, "Events size is ${eventsList?.size}")
                val user = FirebaseManager.getUser()
                events.clear()
                featured.events.clear()
                eventSize = 0
                if(eventsList != null) {
                    eventSize = eventsList.size

                    eventsList.forEach {
                        if(myEvents) {
                            if(it.creator.uid == user?.uid) events.add(it)
                        } else {
                            if(featuredTypes.contains(it.eventType) && froshGroup.contains(studentId)) {
                                featured.events.add(it)
                            } else if (!featuredTypes.contains(it.eventType)) {
                                events.add(it)
                            }
                        }
                    }
                }

                copyAds(eventSize)
                if(featured.events.isNotEmpty()) {
                    events.add(0, featured)
                }
                //event_recycler.stopScroll()
                withContext(Dispatchers.Main) {
                    adapter?.notifyListChanged(events)
                    updateUiVisibility()
                }
            }
        })
    }

    private fun observeFrosh() {
        viewModel.getFroshGroup().observe(this, Observer {
            if (it != null) {
                froshGroup.clear()
                froshGroup.addAll(it)
            }
        })
    }

    private fun observePlaces() {
        places?.observe(this, Observer {
            it?.let {
                adapter?.notifyPlaceChanged(it)
            }
        })
    }

    private fun observeAds() {
        if(!configProvider.isAdsEventsFragmentEnabled()) return
        Log.d(TAG, "Observing ads")
        viewModel.getAds().observe(this, Observer {
            if (it != null){
                ads.clear()
                ads.addAll(it)
                copyAds(eventSize)
                //event_recycler.stopScroll()
                adapter?.notifyListChanged(events)
            }
        })
    }

    companion object {
        private const val UNIVERSITY_PARAM = "university"
        private const val MY_EVENTS_PARAM = "myEvents"
        fun newInstance(university: String, myEvents: Boolean = false) = EventsFragment().apply {
            arguments = Bundle().apply {
                putString(UNIVERSITY_PARAM, university)
                putBoolean(MY_EVENTS_PARAM, myEvents)
            }
        }
    }
}