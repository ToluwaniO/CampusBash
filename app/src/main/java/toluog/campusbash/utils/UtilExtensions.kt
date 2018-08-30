package toluog.campusbash.utils

import com.stripe.android.model.Card
import org.json.JSONObject

val CARD_NUMBER = "card_number"

fun Card.convertToJson(): JSONObject? {
    return toJson().apply {
        put(CARD_NUMBER, number)
    }
}

fun String.isValidInt() = this.toDouble() <= Int.MAX_VALUE && this.toDouble() >= Int.MIN_VALUE

fun String.isValidLong() = this.toDouble() <= Long.MAX_VALUE && this.toDouble() >= Long.MIN_VALUE
