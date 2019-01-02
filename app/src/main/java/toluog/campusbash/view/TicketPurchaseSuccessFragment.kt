package toluog.campusbash.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_ticket_purchase_success.*
import toluog.campusbash.R
import toluog.campusbash.utils.extension.act


class TicketPurchaseSuccessFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ticket_purchase_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        explore_events_button.setOnClickListener {
            act.finish()
        }
    }

    companion object {
        fun newInstance() = TicketPurchaseSuccessFragment()
    }
}
