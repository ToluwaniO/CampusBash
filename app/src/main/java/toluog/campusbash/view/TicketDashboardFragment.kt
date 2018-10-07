package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bignerdranch.expandablerecyclerview.ChildViewHolder
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter
import com.bignerdranch.expandablerecyclerview.ParentViewHolder
import com.bignerdranch.expandablerecyclerview.model.Parent
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_ticket_dashboard.*
import kotlinx.android.synthetic.main.user_ticket_child_layout.*
import kotlinx.android.synthetic.main.user_ticket_parent_layout.*

import toluog.campusbash.R
import toluog.campusbash.model.dashboard.TicketQuantity
import toluog.campusbash.model.dashboard.UserTicket
import toluog.campusbash.utils.AppContract
import toluog.campusbash.view.viewmodel.EventDashboardViewModel

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TicketDashboardFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TicketDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class TicketDashboardFragment : Fragment() {

    private var eventId: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var viewModel: EventDashboardViewModel
    private lateinit var adapter: UserTicketAdapter
    private val userTickets = arrayListOf<UserTicket>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ticket_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UserTicketAdapter(userTickets)
        user_ticket_recycler.layoutManager = LinearLayoutManager(activity)
        user_ticket_recycler.adapter = adapter
        arguments?.let {
            eventId = it.getString(AppContract.EVENT_ID)
        }
        Log.d(TAG, "Event id = $eventId")


        viewModel = ViewModelProviders.of(activity!!).get(EventDashboardViewModel::class.java)
        viewModel.getUsersWithTickets(eventId ?: "")?.observe(this, Observer {
            Log.d(TAG, "Tickets = $it")
            userTickets.clear()
            it?.let {
                userTickets.addAll(it)
            }
            adapter.notifyParentDataSetChanged(true)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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

    inner class UserTicketAdapter(var userTickets: ArrayList<UserTicket>):
            ExpandableRecyclerAdapter<UserTicket, TicketQuantity, UserTicketAdapter.UserViewHolder,
                    UserTicketAdapter.TicketsViewHolder>(userTickets) {
        override fun onCreateParentViewHolder(parentViewGroup: ViewGroup, viewType: Int): UserViewHolder {
            val v = LayoutInflater.from(parentViewGroup.context)
                    .inflate(R.layout.user_ticket_parent_layout, parentViewGroup, false)
            return UserViewHolder(v)
        }

        override fun onBindChildViewHolder(childViewHolder: TicketsViewHolder, parentPosition: Int, childPosition: Int, child: TicketQuantity) {
            childViewHolder.bind(userTickets[parentPosition].quantities[childPosition])
        }

        override fun onBindParentViewHolder(parentViewHolder: UserViewHolder, parentPosition: Int, parent: UserTicket) {
            parentViewHolder.bind(userTickets[parentPosition])
        }

        override fun onCreateChildViewHolder(childViewGroup: ViewGroup, viewType: Int): TicketsViewHolder {
            val v = LayoutInflater.from(childViewGroup.context)
                    .inflate(R.layout.user_ticket_child_layout, childViewGroup, false)
            return TicketsViewHolder(v)
        }

        inner class UserViewHolder(override val containerView: View) :
                ParentViewHolder<Parent<UserTicket>, UserTicket>(containerView), LayoutContainer {
            fun bind(userTicket: UserTicket) {
                if(userTicket.buyerName.isBlank()) {
                    buyer_name.text = userTicket.buyerEmail
                    buyer_email.visibility = View.GONE
                } else {
                    buyer_name.text = userTicket.buyerName
                    buyer_email.text = userTicket.buyerEmail
                }
                val ticketNo: String = if(userTicket.quantity > 1) {
                    containerView.context.getString(R.string.ticket_quantity_with_params, userTicket.quantity)
                } else {
                    containerView.context.getString(R.string.ticket_quantity_one)
                }
                bought_quantity.text = ticketNo

                dropdown_button.setOnClickListener {
                    if(isExpanded) {
                        collapseView()
                        dropdown_button.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
                    } else {
                        expandView()
                        dropdown_button.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp)
                    }
                }
            }
        }

        inner class TicketsViewHolder(override val containerView: View) : ChildViewHolder<TicketQuantity>(containerView),
                LayoutContainer {
            fun bind(ticketQuantity: TicketQuantity) {
                ticket_name.text = ticketQuantity.name
                ticket_quantity.text = ticketQuantity.quantity.toString()
            }
        }

    }

    companion object {
        private val TAG = TicketDashboardFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(eventId: String) = TicketDashboardFragment().apply {
            arguments = Bundle().apply {
                putString(AppContract.EVENT_ID, eventId)
            }
        }
    }
}
