package toluog.campusbash.view

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.price_breakdown_layout.*
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import java.math.BigDecimal

class PriceBreakDownDialogFragment: DialogFragment() {

    private lateinit var priceBreakdown: HashMap<String, BigDecimal>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.price_breakdown_layout, container, false)
        priceBreakdown = arguments?.get(AppContract.PRICE_BREAKDOWN) as HashMap<String, BigDecimal>
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateView()
    }

    private fun updateView() {
        ticket_fee.text = getString(R.string.price_value, "$",
                priceBreakdown[AppContract.TICKET_FEE])
        service_fee.text = getString(R.string.price_value, "$",
                priceBreakdown[AppContract.SERVICE_FEE])
        payment_fee.text = getString(R.string.price_value, "$",
                priceBreakdown[AppContract.PAYMENT_FEE])
        total_fee.text = getString(R.string.price_value, "$",
                priceBreakdown[AppContract.PRE_TAX_FEE])
    }
}