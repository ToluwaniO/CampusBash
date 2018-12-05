package toluog.campusbash.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.no_events_layout.*
import kotlinx.android.synthetic.main.search_event_layout.*
import kotlinx.coroutines.*
import toluog.campusbash.R
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Event
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.extension.act
import toluog.campusbash.view.viewmodel.EventsViewModel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by oguns on 3/17/2018.
 */
class SearchEventFragment: Fragment(), DatePickerFragment.DateSetListener {

    private val TAG = SearchEventFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var viewModel: EventsViewModel
    private val days = listOf(ChipData("Today", false),
            ChipData("Tomorrow", false), ChipData("The weekend", false),
            ChipData("Pick a date", false))
    private val eventTypes = ArrayList<ChipData>()
    private val events = ArrayList<Any>()
    private var liveEvents: LiveData<List<Event>>? = null
    private val queryMap = ArrayMap<String, Any?>()
    private lateinit var eventAdapter: EventAdapter
    private var date: Long? = null
    private var pickDate = false
    private val threadJob = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val threadScope = CoroutineScope(threadJob)

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

        for (day in days) {
            val chip = createChip(day.title)
            date_chip_group.addView(chip)
        }
        eventTypes.sortWith(compareBy {it.title})
        for (type in eventTypes) {
            val chip = createChip(type.title)
            type_chip_group.addView(chip)
        }
        eventAdapter = EventAdapter(events, emptyList(), rootView.context)
        event_recycler.adapter = eventAdapter
        event_recycler.layoutManager = LinearLayoutManager(rootView.context)

        search_bar.addTextChangedListener(searchWatcher)
        setDateGroupListener()
        setEventTypeGroupListener()
    }

    override fun onDestroyView() {
        threadJob.cancel()
        super.onDestroyView()
    }

    private fun setDateGroupListener() {
        date_chip_group.setOnCheckedChangeListener { chipGroup, i ->
            val cal = Calendar.getInstance()
            val chip = chipGroup.findViewById<Chip>(i)

            if (chip == null) {
                queryMap.remove("time")
                return@setOnCheckedChangeListener
            }

            when(chip.text) {
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
                    pickDate = true
                    val dialog = DatePickerFragment()
                    dialog.setOnDateSetListener(object : DatePickerFragment.DateSetListener {
                        override fun dateChanged(year: Int, month: Int, dayOfMonth: Int) {
                            this@SearchEventFragment.dateChanged(year, month, dayOfMonth)
                        }
                    })
                    dialog.show(activity?.supportFragmentManager, null)
                }
            }
            queryMap["time"] = date
        }
    }

    private fun setEventTypeGroupListener() {
        type_chip_group.setOnCheckedChangeListener { chipGroup, i ->
            val chip = chipGroup.findViewById<Chip>(i)

            if (chip == null) {
                queryMap.remove("type")
                observeEvents()
                return@setOnCheckedChangeListener
            }
            queryMap["type"] = chip.text
            observeEvents()
        }
    }

    override fun dateChanged(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, 0, 0, 0)
        date = calendar.timeInMillis
        queryMap["time"] = date
        Log.d(TAG, "Date is ${Util.formatDateTime(calendar)}")
        observeEvents()
    }

    private fun observeEvents() {
        val type = queryMap["type"] as String?
        val time = queryMap["time"] as Long?
        val text = if(queryMap.contains("text")) {
            queryMap["text"] as String
        } else {
            ""
        }

        if(text.isEmpty() && type == null && time == null) {
            events.clear()
            updateRecyclers()
            if (events.size > 0) {
                eventAdapter.notifyDataSetChanged()
            }
            return
        }

        Log.d(TAG, "QUERY -> $queryMap")

        liveEvents = if(type != null) {
            viewModel.getEvents("%$text%", type, time ?: System.currentTimeMillis())
        } else {
            viewModel.getEvents("%$text%", time ?: System.currentTimeMillis())
        }
        liveEvents?.observe(this, Observer {
            threadScope.launch {
                events.clear()
                if(it != null) {
                    Log.d(TAG, "query size -> ${it.size}")
                    it.forEach {event ->
                        if(time != null) {
                            val rangeB = TimeUnit.HOURS.toMillis(24)+time
                            if (Util.dateRangeCheck(event.startTime, time, rangeB) &&
                                    Util.dateRangeCheck(event.endTime, time, rangeB)) {
                                events.add(event)
                            }
                        }
                        else {
                            events.add(event)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    updateRecyclers()
                    if (events.size > 0) {
                        eventAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
        viewModel.getPlaces()?.observe(this, Observer {
            if (it != null) {
                eventAdapter.notifyPlaceChanged(it)
            }
        })
    }

    private fun updateRecyclers() {
        if(events.size > 0) {
            no_events.visibility = View.GONE
            event_recycler.visibility = View.VISIBLE
        } else {
            no_events.visibility = View.VISIBLE
            event_recycler.visibility = View.GONE
        }
    }

    private fun createChip(title: String) = Chip(type_chip_group.context).apply {
        text = title
        setTextColor(ContextCompat.getColor(this@SearchEventFragment.act, android.R.color.white))
        isClickable = true
        isCheckable = true
        isFocusable = true
        checkedIcon = null
        setChipBackgroundColorResource(R.color.search_chip_list)
    }

    data class ChipData(var title: String, var selected: Boolean)

}