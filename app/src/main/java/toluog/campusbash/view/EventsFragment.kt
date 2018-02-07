package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide.init
import com.google.android.gms.ads.formats.NativeAd
import kotlinx.android.synthetic.main.events_layout.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.support.v4.intentFor
import toluog.campusbash.R
import toluog.campusbash.R.id.event_recycler
import toluog.campusbash.utils.AppContract
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

/**
 * Created by oguns on 12/13/2017.
 */
class EventsFragment() : Fragment(){

    private var rootView: View? = null
    private var myEvents = false
    private val TAG = EventsFragment::class.java.simpleName
    private var adapter: EventAdapter? = null
    private val events: ArrayList<Any> = ArrayList()
    private var isMine = false
    private val ads = ArrayList<NativeAd>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.events_layout, container, false)

        val bundle = this.arguments
        if(bundle != null){
            myEvents = bundle.getBoolean(AppContract.MY_EVENT_BUNDLE)
        }

        val viewModel: EventsViewModel = ViewModelProviders.of(activity).get(EventsViewModel::class.java)
        viewModel.getEvents()?.observe(this, Observer { eventsList ->
            val user = FirebaseManager.getUser()
            events.clear()
            eventsList?.forEach {
                if(myEvents) {
                    if(it.creator.uid == user?.uid) events.add(it)
                } else {
                    events.add(it)
                }
                copyAds()
            }

            adapter?.notifyDataSetChanged()
        })

        viewModel.getAds().observe(this, Observer {
            if (it != null){
                ads.clear()
                it.forEach { ad ->
                    ads.add(ad)
                }
                copyAds()
                adapter?.notifyDataSetChanged()
            }
        })

        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        adapter = EventAdapter(events, rootView?.context)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(rootView?.context)
        event_recycler.layoutManager = layoutManager
        event_recycler.itemAnimator = DefaultItemAnimator()
        event_recycler.adapter = adapter
    }

    private fun copyAds() {
        if(events.size < AppContract.MAX_EVENTS_FOR_ADS_IN_FRAGMENT || ads.size < AppContract.NUM_EVENTS_FRAGMENT_ADS) return
        clearAds()
        val offset = events.size / ads.size + 1
        var index = 2
        Log.d(TAG, "AD NUMBER: ${ads.size}")
        for (ad in ads) {
            if(index < events.size && events[index] !is NativeAd){
                events.add(index, ad)
                Log.d(TAG, "Adding to position $index")
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
                    Log.d(TAG, "Removimg from position $it")
                    events.remove(it)
                }
    }
}