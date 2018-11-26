package toluog.campusbash.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.stripe.android.model.Card
import kotlinx.android.synthetic.main.activity_add_card.*
import toluog.campusbash.R
import toluog.campusbash.model.BashCard
import toluog.campusbash.utils.extension.alertDialog
import toluog.campusbash.utils.extension.snackbar
import toluog.campusbash.view.CardPaymentActivity.Companion.ADD_CARD

class AddCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_card_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_done -> addCard()
        }
        return true
    }

    override fun onBackPressed() {
        val dialog = alertDialog(getString(R.string.exit_without_saving_card))
        dialog.positiveButton(getString(R.string.yes)) {
            super.onBackPressed()
        }
        dialog.negativeButton(getString(R.string.no)) {
            it.dismiss()
        }
        dialog.show()
    }

    private fun addCard() {
        val card = card_input_widget.card
        if(card != null && validateCard(card)) {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("card", BashCard(null, card, true))
            })
            finish()
        } else if(card == null) {
            root_view.snackbar(R.string.could_not_validate_card)
        }
    }

    private fun validateCard(cardToSave: Card): Boolean {
        if(!cardToSave.validateCard()) {
            root_view.snackbar(R.string.could_not_validate_card)
            return false
        }
        return true
    }
}
