package toluog.campusbash.view

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.robertlevonyan.views.chip.Chip
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.chip_layout.*
import kotlinx.android.synthetic.main.no_events_layout.*
import kotlinx.android.synthetic.main.search_event_layout.*
import toluog.campusbash.R
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.utils.DbQueryBuilder

/**
 * Created by oguns on 3/17/2018.
 */
class SearchEventFragment: Fragment() {

    private val TAG = SearchEventFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var viewModel: EventsViewModel
    private val days = arrayListOf<String>("Today", "Tomorrow", "The weekend", "Pick a date")
    private lateinit var eventTypes: List<String>
    private val events = ArrayList<Any>()
    private var liveEvents: LiveData<List<Event>>? = null
    private val queryMap = HashMap<String, String>()
    private lateinit var eventAdapter: EventAdapter

    val searchWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(s != null) {
                val query = s.toString()
                queryMap["text"] = query
                observeEvents()
                Log.d(TAG, "Query text changed")
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.search_event_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(EventsViewModel::class.java)
        eventTypes = resources.getStringArray(R.array.party_types).asList()
        date_chip_recycler.apply {
            adapter = ChipAdapter(days, 0)
            layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, true)
        }
        type_chip_recycler.apply {
            adapter = ChipAdapter(eventTypes, 1)
            layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, true)
        }
        eventAdapter = EventAdapter(events, rootView.context)
        event_recycler.adapter = eventAdapter
        event_recycler.layoutManager = LinearLayoutManager(rootView.context)
        search_bar.addTextChangedListener(searchWatcher)
    }

    private fun observeEvents() {
        val type = queryMap["type"]
        Log.d(TAG, "QUERY -> $queryMap")
        liveEvents = if(type != null) {
            viewModel.getEvents("%${queryMap["text"]}%", type, queryMap["time"]?.toLong()
                    ?: System.currentTimeMillis())
        } else {
            viewModel.getEvents("%${queryMap["text"]}%", queryMap["time"]?.toLong()
                    ?: System.currentTimeMillis())
        }
        liveEvents?.observe(this, Observer {
            events.clear()
            if(it != null) {
                Log.d(TAG, "query size -> ${it.size}")
                events.addAll(it)
                if(events.size > 0) {
                    no_events.visibility = View.GONE
                    event_recycler.visibility = View.VISIBLE
                } else {
                    no_events.visibility = View.VISIBLE
                    event_recycler.visibility = View.GONE
                }
            }
            eventAdapter.notifyDataSetChanged()
        })
    }

    private fun resetChips(selected: String, type: Int) {
        val recycler: RecyclerView = if(type == 0) {
            date_chip_recycler
        } else {
            type_chip_recycler
        }
        for (i in 0 until recycler.childCount) {
            val view = recycler.getChildAt(i) as Chip
            if(view.chipText != selected) {
                view.isSelected = false
            }
        }
        if(type == 0) {
            queryMap["time"] = selected
        } else {
            queryMap["type"] = selected
        }
    }

    inner class ChipAdapter(val chipData: List<String>, val type: Int): RecyclerView.Adapter<ChipAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.chip_layout, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount()= chipData.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(chipData[position])
        }

        inner class ViewHolder(override val containerView: View?): RecyclerView.ViewHolder(containerView), LayoutContainer {
            fun bind(title: String) {
                chip.chipText = title

                chip.setOnChipClickListener {
                    resetChips(title, type)
                    observeEvents()
                    Log.d(TAG, "Chip clicked")
                }
            }


        }
    }
}