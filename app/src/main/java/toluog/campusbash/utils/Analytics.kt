package toluog.campusbash.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import toluog.campusbash.model.Event

object Analytics {
    lateinit var analytics: FirebaseAnalytics
    var isAnalyticsInitialized = false

    private const val EVENT_SELECTED = "event_selected"
    private const val ONBOARD_FINISHED = "onboard_finished"
    private const val BUY_TICKET_CLICKED = "buy_ticket_clicked"
    private const val TICKET_BOUGHT = "ticket_bought"
    private const val TICKET_BOUGHT_FAILED = "ticket_bought_failed"

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
        isAnalyticsInitialized = true
    }

    fun logEventSelected(event: Event) {
        if(!isAnalyticsInitialized) {
            throw Exception("Analytics is not initialized!")
        }
        val bundle = getEventParams(event)
        analytics.logEvent(EVENT_SELECTED, bundle)
    }

    fun logOnBoardFinished() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, FirebaseManager.getUser()?.uid ?: "null id")
        }
        analytics.logEvent(ONBOARD_FINISHED, bundle)
    }

    fun logBuyTicketClicked(event: Event) {
        analytics.logEvent(BUY_TICKET_CLICKED, getEventParams(event))
    }

    fun logTicketBought(event: Event) {
        analytics.logEvent(TICKET_BOUGHT, getEventParams(event))
    }

    fun logTicketBoughtFailed(event: Event) {
        analytics.logEvent(TICKET_BOUGHT_FAILED, getEventParams(event))
    }

    private fun getEventParams(event: Event): Bundle {
        return Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, event.eventId)
            putString(FirebaseAnalytics.Param.ITEM_NAME, event.eventName)
            putString(FirebaseAnalytics.Param.LOCATION, event.university)
        }
    }
}