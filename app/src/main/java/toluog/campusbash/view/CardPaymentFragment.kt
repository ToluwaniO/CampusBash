package toluog.campusbash.view


import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.wallet.*
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.add_new_card_button.*
import kotlinx.android.synthetic.main.credit_card_view.*
import kotlinx.android.synthetic.main.fragment_card_payment.*
import kotlinx.coroutines.*
import toluog.campusbash.R
import toluog.campusbash.model.BashCard
import toluog.campusbash.model.TicketPriceBreakdown
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.CampusBash
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.extension.*
import toluog.campusbash.view.viewmodel.ViewEventViewModel
import java.lang.Exception
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class CardPaymentFragment : BaseFragment() {

    private val TAG = CardPaymentActivity::class.java.simpleName
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 3731
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var googlePayAlert: AndroidAlertBuilder
    private lateinit var currency: String
    private val cards = ArrayList<BashCard>()
    private val adapter = CardAdapter()
    private var pleaseWait: ProgressDialog? = null
    private lateinit var viewModel: ViewEventViewModel

    private var breakdown: TicketPriceBreakdown? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_card_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(activity!!).get(ViewEventViewModel::class.java)
        val price = CardPaymentFragmentArgs.fromBundle(arguments).price
        loadBreakdown(price)

    }

    private fun loadBreakdown(price: Int) {
        this.launch {
            val bd = viewModel.getTicketBreakdown(price)
            withContext(Dispatchers.Main) {
                if (bd != null) {
                    initCardAndPricing(bd)
                    progress_bar.visibility = View.GONE
                    main_layout.visibility = View.VISIBLE
                } else {
                    container.indefiniteSnackbar(R.string.error_occurred, R.string.retry) {
                        main_layout.visibility = View.GONE
                        progress_bar.visibility = View.VISIBLE
                        this@CardPaymentFragment.launch { loadBreakdown(price) }
                    }
                }
            }
        }
    }

    private fun initCardAndPricing(breakdown: TicketPriceBreakdown) {
        this.breakdown = breakdown
        currency = viewModel.currency ?: "CB$"
        manageCards()

        pleaseWait = act.indeterminateProgressDialog(R.string.please_wait)
        pleaseWait?.dismiss()
        googlePayAlert = act.alertDialog(getString(R.string.use_google_pay))
        googlePayAlert.positiveButton(getString(R.string.yes)) {
            Log.d(TAG, "Yes clicked for Google Pay")
            val request = viewModel.createPaymentDataRequest()
            AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(request), act,
                    LOAD_PAYMENT_DATA_REQUEST_CODE)
        }
        googlePayAlert.negativeButton(getString(R.string.no)) {
            Log.d(TAG, "User returned no for google pay request")
            updateView()
        }

        val environment = if(Util.devFlavor()) {
            WalletConstants.ENVIRONMENT_TEST
        } else {
            WalletConstants.ENVIRONMENT_PRODUCTION
        }
        paymentsClient = Wallet.getPaymentsClient(act,
                Wallet.WalletOptions.Builder().setEnvironment(environment)
                        .build())

        card_recycler.adapter = adapter
        val layoutManager = LinearLayoutManager(act)
        card_recycler.layoutManager = layoutManager

        main_currency.text = currency
        ticket_fee.text = getString(R.string.one_ticket_price, breakdown.ticketFee.toDouble()/100)
        service_fee.text = getString(R.string.one_ticket_price, breakdown.serviceFee.toDouble()/100)
        payment_fee.text = getString(R.string.one_ticket_price, breakdown.paymentFee.toDouble()/100)
        total_fee.text = getString(R.string.one_ticket_price, breakdown.totalFee.toDouble()/100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if(data != null) {
                            val paymentData = PaymentData.getFromIntent(data)
                            //val cardInfo = paymentData?.cardInfo
                            paymentData?.paymentMethodToken?.token
                            //val address = paymentData?.shippingAddress
                            val rawToken = paymentData?.paymentMethodToken?.token
                            val stripeToken = Token.fromString(rawToken)
                            if (stripeToken != null) {
                                // This chargeToken function is a call to your own server, which should then connect
                                // to Stripe's API to finish the charge.
                                buyTicket(stripeToken.id, true)
                            }
                        }
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        Log.d(TAG, "Google pay error\ne -> ${status.toString()}")
                    }
                    Activity.RESULT_CANCELED -> Log.d(TAG, "Google Pay result cancelled")
                }
            }
            ADD_CARD -> {
                if(resultCode == Activity.RESULT_OK) {
                    val bashCard = data?.extras?.get("card") as BashCard?
                    if (bashCard != null) {
                        getToken(bashCard)
                    }
                }
            }
        }
    }

    private fun getToken(bashCard: BashCard) {
        pleaseWait?.show()
        val source = bashCard.customerSource
        val card = bashCard.card
        when {
            source != null -> buyTicket(source.id, false)
            card != null -> {
                val stripe = Stripe(act, AppContract.STRIPE_PUBLISHABLE_KEY)
                stripe.createToken(card, object : TokenCallback {
                    override fun onSuccess(token: Token?) {
                        val id: String? = token?.id
                        buyTicket(id, bashCard.newCard)
                    }

                    override fun onError(error: Exception?) {
                        Log.d(TAG, "Token error\ne -> ${error?.message}")
                        buyTicket(null, false)
                    }
                })
            }
            else -> {
                Log.d(TAG, "Card is null")
                buyTicket(null, false)
            }
        }
    }

    private fun buyTicket(tokenId: String?, newCard: Boolean) {
        pleaseWait?.show()
        this.launch {
            val state = viewModel.buyTickets(tokenId, newCard)
            pleaseWait?.dismiss()
            when (state) {
                is ViewEventViewModel.BuyTicketState.Success -> {
                    findNavController().navigate(CardPaymentFragmentDirections
                            .actionCardPaymentDestinationToTicketPurchaseSuccessFragment())
                }
                is ViewEventViewModel.BuyTicketState.NotSignedIn -> {
                    Util.startSignInActivity(act)
                }
                is ViewEventViewModel.BuyTicketState.QuantityIsZero -> {
                    container.snackbar(R.string.no_ticket_purchased)
                }
                is ViewEventViewModel.BuyTicketState.NoPaymentMethod -> {
                    container.snackbar(R.string.could_not_validate_card)
                }
                is ViewEventViewModel.BuyTicketState.Error -> {
                    container.snackbar(R.string.error_occurred)
                }
            }
        }
    }

    override fun onDestroy() {
        pleaseWait?.dismiss()
        super.onDestroy()
    }

    private fun updateView() {
        if(cards.size > 0) {
            adapter.notifyDataSetChanged()
            card_recycler.visibility = View.VISIBLE
//            no_card_layout.visibility = View.GONE
        } else {
            card_recycler.visibility = View.GONE
//            no_card_layout.visibility = View.VISIBLE
        }
    }

    private fun manageCards() {
        CampusBash.getBashCards().observe(this, Observer {
            cards.removeAll(cards.filter { !it.newCard })
            if(it != null) {
                cards.addAll(it)
            }
            updateView()
        })
    }

    companion object {
        const val ADD_CARD = 3628
    }

    inner class CardAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val CARD_VIEW_TYPE = 0
        private val ADD_CARD_VIEW_TYPE = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == CARD_VIEW_TYPE) {
                val view = LayoutInflater.from(act)
                        .inflate(R.layout.credit_card_view, parent, false)
                BashCardViewHolder(view)
            } else {
                val view = LayoutInflater.from(act)
                        .inflate(R.layout.add_new_card_button, parent, false)
                AddCardViewHolder(view)
            }
        }

        override fun getItemCount() = cards.size+1

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (position == cards.size) {
                (holder as AddCardViewHolder).bind()
            } else {
                (holder as BashCardViewHolder).bind(cards[position])
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position == cards.size) {
                return ADD_CARD_VIEW_TYPE
            }
            return CARD_VIEW_TYPE
        }

        inner class BashCardViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
                LayoutContainer {

            fun bind(bCard: BashCard) {
                val card = bCard.customerSource?.asCard() ?: bCard.card
                card_number.text = getString(R.string.debit_card_digits, card?.brand, card?.last4)
                val logo = Card.BRAND_RESOURCE_MAP[card?.brand]
                val unknown = Card.BRAND_RESOURCE_MAP[Card.UNKNOWN]
                if(logo != null) {
                    logo_view.setImageResource(logo)
                } else if(unknown != null) {
                    logo_view.setImageResource(unknown)
                }
                containerView.setOnClickListener {
                    getToken(bCard)
                }
            }

        }

        inner class AddCardViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
                LayoutContainer {
            fun bind() {
                add_new_card.setOnClickListener {
                    startActivityForResult(intentFor<AddCardActivity>(), ADD_CARD)
                }
            }
        }

    }

}
