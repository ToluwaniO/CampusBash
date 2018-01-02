package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide.init
import kotlinx.android.synthetic.main.events_layout.*
import org.jetbrains.anko.support.v4.intentFor
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event

/**
 * Created by oguns on 12/13/2017.
 */
class EventsFragment() : Fragment(){

    private var rootView: View? = null
    private var myEvents = false
    private val TAG = EventsFragment::class.java.simpleName
    private var adapter: EventAdapter? = null
    private val events: ArrayList<Event> = ArrayList()
    private var isMine = false

    init {

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = arguments
        if(bundle != null){
            myEvents = bundle.getBoolean(AppContract.MY_EVENT_BUNDLE)
        }

        rootView = inflater?.inflate(R.layout.events_layout, container, false)


        val viewModel: EventsViewModel = ViewModelProviders.of(activity).get(EventsViewModel::class.java)
        viewModel.getEvents()?.observe(this, Observer { eventsList ->
            events.clear()
            eventsList?.forEach {
                events.add(it)
                Log.d(TAG, it.toString())
            }
            adapter?.notifyDataSetChanged()
        })

        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        adapter = EventAdapter(events, rootView?.context)
        val layoutManager : RecyclerView.LayoutManager = GridLayoutManager(rootView?.context, 1)
        event_recycler.layoutManager = layoutManager
        event_recycler.itemAnimator = DefaultItemAnimator()
        event_recycler.adapter = adapter
    }
}