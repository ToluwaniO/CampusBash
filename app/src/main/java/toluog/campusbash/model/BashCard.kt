package toluog.campusbash.model

import com.stripe.android.model.Card
import com.stripe.android.model.CustomerSource

data class BashCard(var customerSource: CustomerSource?, var card: Card? = null, var newCard: Boolean = false)