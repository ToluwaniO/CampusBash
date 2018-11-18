package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_event_dashboard_layout.*
import kotlinx.android.synthetic.main.tickets_sold_chart.*

import toluog.campusbash.R
import toluog.campusbash.model.Event
import toluog.campusbash.model.Ticket
import toluog.campusbash.model.dashboard.UserTicket
import toluog.campusbash.utils.AppContract
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.ViewPortHandler
import toluog.campusbash.view.viewmodel.EventDashboardViewModel

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [EventDashboardFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [EventDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class EventDashboardFragment : Fragment() {
    private val TAG = EventDashboardFragment::class.java.simpleName
    private var eventId: String = ""
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var viewModel: EventDashboardViewModel
    private val colors = arrayListOf<Int>()

    private var event: Event? = null
    private val tickets = ArrayList<Ticket>()
    private val userTickets = arrayListOf<UserTicket>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(AppContract.EVENT_ID) ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_dashboard_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateColors()
        ticket_chart_recycler.layoutManager = LinearLayoutManager(activity)
        ticket_chart_recycler.adapter = ChartAdapter()

        viewModel = ViewModelProviders.of(activity!!).get(EventDashboardViewModel::class.java)

        viewModel.getEvent(eventId)?.observe(this, Observer {
            this.event = it
            updateView()
        })

        viewModel.getEventTickets(eventId)?.observe(this, Observer {

        })

        viewModel.getUsersWithTickets(eventId)?.observe(this, Observer {
            userTickets.clear()
            if(it != null) {
                userTickets.addAll(it)
            }
            updateView()
        })
    }

    private fun updateView() {
        var totalMade = 0.0
        var ticketQuantity = 0L
        var ticketsSold = 0L

        tickets.forEach {
            ticketsSold += it.ticketsSold
            ticketQuantity += it.quantity
        }
        userTickets.forEach {
            totalMade += it.totalPrice
        }

        total_made.text = getString(R.string.price, "$", totalMade)
        tickets_sold.text = getString(R.string.ticket_sold_available, ticketsSold, ticketQuantity)
        val entries = listOf(PieEntry(ticketsSold.toFloat(), "sold"),
                PieEntry(ticketQuantity.toFloat(), "quantity"))
        setChart(pie_chart, entries)
        ticket_chart_recycler.adapter?.notifyDataSetChanged()
    }

    private fun updateColors() {
        // add a lot of colors
        for (c in ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS)
            colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS)
            colors.add(c)

        colors.add(ColorTemplate.getHoloBlue())
    }

    private fun setChart(chart: PieChart, entries: List<PieEntry>) {
        val set = PieDataSet(entries, "Tickets")
        set.setDrawIcons(false)
        set.sliceSpace = 3f
        set.iconsOffset = MPPointF(0f, 40f)
        set.selectionShift = 5f

        set.colors = colors

        // undo all highlights
        chart.highlightValues(null)

        val data = PieData(set)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setValueFormatter(DecimalRemover())
        chart.description.isEnabled = false
        chart.data = data
        chart.holeRadius = 0f
        chart.transparentCircleRadius = 0f
        chart.setDrawEntryLabels(false)
        chart.setUsePercentValues(false)
        chart.setDrawCenterText(false)
        chart.isRotationEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    inner class ChartAdapter: RecyclerView.Adapter<ChartAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.tickets_sold_chart, parent,
                    false)
            return ViewHolder(v)
        }

        override fun getItemCount() = tickets.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val ticket = tickets.get(position)
            holder.bind(ticket)
        }

        inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
                LayoutContainer {

            fun bind(ticket: Ticket) {
                Log.d(TAG, "$ticket")
                ticket_name.text = ticket.name
                tickets_sold.text = getString(R.string.ticket_sold_available, ticket.ticketsSold,
                        ticket.quantity)
                val entries = listOf(PieEntry(ticket.ticketsSold.toFloat(), "sold"),
                        PieEntry(ticket.quantity.toFloat(), "quantity"))
                setChart(pie_chart, entries)
            }

        }

    }

    inner class DecimalRemover: PercentFormatter() {

        override fun getFormattedValue(value: Float, entry: Entry, dataSetIndex: Int, viewPortHandler: ViewPortHandler): String {
            return ""
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param eventId Parameter 1.
         * @return A new instance of fragment EventDashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(eventId: String) =
                EventDashboardFragment().apply {
                    arguments = Bundle().apply {
                        putString(AppContract.EVENT_ID, eventId)
                    }
                }
    }
}
