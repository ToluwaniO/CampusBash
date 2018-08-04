package toluog.campusbash.utils

import com.stripe.android.model.Card
import org.json.JSONObject

val CARD_NUMBER = "card_number"

fun Card.convertToJson(): JSONObject? {
    return toJson().apply {
        put(CARD_NUMBER, number)
    }
}
