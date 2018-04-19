package toluog.campusbash.view

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
import org.jetbrains.anko.coroutines.experimental.bg
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.utils.ConfigProvider
import toluog.campusbash.utils.FirebaseManager

/**
 * Created by oguns on 12/13/2017.
 */
class EventsFragment() : Fragment(){

    private lateinit var rootView: View
    private var myEvents = false
    private val TAG = EventsFragment::class.java.simpleName
    private lateinit var viewModel: EventsViewModel
    private val configProvider = ConfigProvider(FirebaseRemoteConfig.getInstance())
    private var adapter: EventAdapter? = null
    private val events: ArrayList<Any> = ArrayList()
    private val ads = ArrayList<NativeAd>()
    private var eventSize = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.events_layout, container, false)

        val bundle = this.arguments
        if(bundle != null){
            myEvents = bundle.getBoolean(AppContract.MY_EVENT_BUNDLE)
        }
        viewModel = ViewModelProviders.of(activity!!).get(EventsViewModel::class.java)
        viewModel.getEvents()?.observe(this, Observer { eventsList ->
            val user = FirebaseManager.getUser()
            events.clear()
            if(eventsList != null) {
                eventSize = eventsList.size

                eventsList.forEach {
                    if(myEvents) {
                        if(it.creator.uid == user?.uid) events.add(it)
                    } else {
                        events.add(it)
                    }
                }
            }

            copyAds(eventSize)
            event_recycler.stopScroll()
            adapter?.notifyListChanged(events)
        })

        if(configProvider.isAdsEventsFragmentEnabled()) {
            Log.d(TAG, "Observing ads")
            viewModel.getAds().observe(this, Observer {
                if (it != null){
                    ads.clear()
                    ads.addAll(it)
                    copyAds(eventSize)
                    event_recycler.stopScroll()
                    adapter?.notifyListChanged(events)
                }
            })
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setNestedScrollingEnabled(event_recycler, !myEvents)
        adapter = EventAdapter(ArrayList<Any>(), rootView.context, myEvents)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(rootView.context)
        event_recycler.layoutManager = layoutManager
        event_recycler.itemAnimator = DefaultItemAnimator()
        event_recycler.adapter = adapter
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
}