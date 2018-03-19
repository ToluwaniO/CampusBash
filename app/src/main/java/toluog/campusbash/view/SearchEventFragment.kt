package toluog.campusbash.view

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
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
import toluog.campusbash.utils.Util
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by oguns on 3/17/2018.
 */
class SearchEventFragment: Fragment(), DatePickerFragment.DateSetListener {

    private val TAG = SearchEventFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var viewModel: EventsViewModel
    private val days = listOf<ChipData>(ChipData("Today", false),
            ChipData("Tomorrow", false), ChipData("The weekend", false),
            ChipData("Pick a date", false))
    private val eventTypes = ArrayList<ChipData>()
    private val events = ArrayList<Any>()
    private var liveEvents: LiveData<List<Event>>? = null
    private val queryMap = HashMap<String, Any?>()
    private lateinit var eventAdapter: EventAdapter
    private var date: Long? = null

    private val searchWatcher = object : TextWatcher {
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
        resources.getStringArray(R.array.party_types).asList().forEach { eventTypes.add(ChipData(it, false)) }

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

    override fun dateChanged(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        date = calendar.timeInMillis
        queryMap["time"] = date
        Log.d(TAG, "Date is ${Util.formatDateTime(calendar)}")
        observeEvents()
    }

    private fun observeEvents() {
        val type = queryMap["type"]
        Log.d(TAG, "QUERY -> $queryMap")
        liveEvents = if(type != null) {
            viewModel.getEvents("%${queryMap["text"]}%", type as String, queryMap["time"] as Long?
                    ?: System.currentTimeMillis())
        } else {
            viewModel.getEvents("%${queryMap["text"]}%", queryMap["time"] as Long?
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

    private fun resetChips(title: String, type: Int, chipData: List<ChipData>) {
        val recycler: RecyclerView = if(type == 0) {
            date_chip_recycler
        } else {
            type_chip_recycler
        }
        for (i in chipData) {
            if(i.title != title && i.selected) {
                i.selected = false
            }
        }
        if(type == 0) {
            getDateFromString(title)
            queryMap["time"] = date
        } else {
            queryMap["type"] = title
        }
        recycler.adapter.notifyDataSetChanged()
    }

    private fun getDateFromString(selected: String) {
        val cal = Calendar.getInstance()
        when(selected) {
            "Today" -> date = System.currentTimeMillis()
            "Tomorrow" -> {
                cal[Calendar.DAY_OF_YEAR] = cal[Calendar.DAY_OF_YEAR] + 1
                cal[Calendar.HOUR_OF_DAY] = 0
                cal[Calendar.MINUTE] = 0
                cal[Calendar.SECOND] = 0
                date = cal.timeInMillis
            }
            "The weekend" -> {
                val delta = 7 - cal[Calendar.DAY_OF_YEAR]
                cal[Calendar.DAY_OF_YEAR] = cal[Calendar.DAY_OF_YEAR] + delta
                cal[Calendar.HOUR_OF_DAY] = 0
                cal[Calendar.MINUTE] = 0
                cal[Calendar.SECOND] = 0
                date = cal.timeInMillis
            }
            "Pick a date" -> {
                val dialog = DatePickerFragment()
                dialog.setOnDateSetListener(object : DatePickerFragment.DateSetListener{
                    override fun dateChanged(year: Int, month: Int, dayOfMonth: Int) {
                        this@SearchEventFragment.dateChanged(year, month, dayOfMonth)
                    }
                })
                dialog.show(activity?.supportFragmentManager, null)
            }
        }
        Log.d(TAG, "Date is ${Util.formatDateTime(cal)}")
    }

    data class ChipData(var title: String, var selected: Boolean)

    inner class ChipAdapter(private val chipData: List<ChipData>, val type: Int): RecyclerView.Adapter<ChipAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.chip_layout, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount()= chipData.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position)
        }

        inner class ViewHolder(override val containerView: View?): RecyclerView.ViewHolder(containerView), LayoutContainer {
            fun bind(position: Int) {
                val chipItem = chipData[position]
                chip.chipText = chipItem.title
                if(chipItem.selected) {
                    chip.changeBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorAccent,
                            null))
                }

                chip.setOnChipClickListener {
                    chipItem.selected = true
                    resetChips(chipData[position].title, type, chipData)
                    observeEvents()
                    Log.d(TAG, "Chip clicked")
                }
            }


        }
    }
}