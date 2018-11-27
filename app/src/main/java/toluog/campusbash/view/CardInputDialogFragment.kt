package toluog.campusbash.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stripe.android.model.Card
import kotlinx.android.synthetic.main.debit_card_dialog_layout.*
import toluog.campusbash.R
import toluog.campusbash.utils.extension.act
import toluog.campusbash.utils.extension.toast
import java.lang.ClassCastException

class CardInputDialogFragment: DialogFragment() {

    interface CardInputListener {
        fun cardReady(card: Card)
    }

    private val TAG = CardInputDialogFragment::class.java.simpleName
    private var rootView: View? = null
    private var callback: CardInputListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.debit_card_dialog_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        save.setOnClickListener {
            saveCard()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            callback = context as CardInputListener?
        } catch (e: ClassCastException) {
            Log.d(TAG, "e -> ${e.message}")
        }
    }

    private fun saveCard() {
        val card = card_input_widget.card
        if(card != null && card.validateCard()) {
            callback?.cardReady(card)
            dismiss()
        } else {
            act.toast(R.string.invalid_card)
        }
    }
}