package toluog.campusbash.utils

import toluog.campusbash.BuildConfig
import toluog.campusbash.model.Creator
import toluog.campusbash.model.LatLng
import toluog.campusbash.model.Ticket

/**
 * Created by oguns on 12/13/2017.
 */
class AppContract{
    companion object {
        val RC_SIGN_IN = 5634
        val MY_EVENT_BUNDLE = "myevent"
        val LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut elementum ante " +
                "vitae risus venenatis blandit. Proin cursus, ante et aliquet lobortis, enim mauris " +
                "pulvinar turpis, eu tincidunt mi orci ac enim. Mauris mattis eget massa nec eleifend." +
                " Morbi faucibus semper gravida. Aenean pharetra tristique eros, sit amet scelerisque " +
                "elit volutpat nec."
        val STANTON_ADDRESS = "9A7, 100 University Private, Ottawa, ON K1N 6N5"
        val STANTON_COORD = LatLng(45.4221, 75.6848)
        val CREATOR = Creator("Toluwani Ogunsanya", "frfgbruf", "ifjihjri")
        val TICKETS = arrayOf(Ticket())

        //FRAGMENT TAGS
        val CREATE_EVENT_TAG = "createevent"
        val CREATE_TICKETS_TAG = "view_tickets_fragment"
        val TICKETS_KEY = "tickets"

        //BUNDLE TAGS
        val TOKEN_ID = "tokenId"
        val NEW_CARD = "newCard"

        //ROOM KEYS
        const val EVENT_TABLE = "Events"
        const val UNIVERSITY_TABLE = "Universities"
        const val CURRENCY_TABLE = "Currencies"

        //FIREBASE KEYS
        val FIREBASE_EVENTS = "events"
        val FIREBASE_UNIVERSITIES = "universities"
        val FIREBASESTORAGE_EVENT_IMAGE_PLACEHOLDERS = "event_placeholder_images"
        val FIREBASE_CURRENCIES = "currencies"
        val FIREBASE_USERS = "users"
        val FIREBASE_FCM_TOKEN = "fcmToken"

        //SHAREDPREFERENCES
        const val PREF_FIRST_OPEN_KEY = "first_open"
        const val PREF_COUNTRY_KEY = "pref_country"
        const val PREF_UNIVERSITY_KEY = "pref_university"
        const val PREF_EVENT_TYPES_KEY = "pref_event_type_set"
        const val PREF_FCM_TOKEN_UPDATED = "pref_fcm_token_updated"

        //ADS
        const val NUM_EVENTS_FRAGMENT_ADS = 5
        const val MAX_EVENTS_FOR_ADS_IN_FRAGMENT = 20

        //RemoteConfig
        val configRefreshTime = 3600L
        get() {
            if(BuildConfig.DEBUG) return 0L
            return field
        }

        val PLACE_AUTOCOMPLETE_REQUEST_CODE = 48547

        //JOB TAGS
        val JOB_EVENT_DELETE = "delete_old_events"

        val STRIPE_PUBLISHABLE_KEY = if(BuildConfig.DEBUG) {
            "pk_test_CVyOXRhK6S5K0RlHkLzIiReJ"
        } else {
            "pk_live_8mv4tYyz8VBXXjUSdmQmOtcD"
        }

        val TICKET_FEE = "ticketFee"
        val PAYMENT_FEE = "paymentFee"
        val SERVICE_FEE = "serviceFee"
        val PRE_TAX_FEE = "preTaxFee"
        val TOTAL_FEE = "totalFee"
        val CAMPUSBASH_CUT = "campusbashCut"
        val CAMPUSBASH_FEE = "campusbashFee"

        val PRICE_BREAKDOWN = "priceBreakdown"

        //NOTIFICATIONS
        const val UNKNOWN_NOTIFICATION = 0
        const val PURCHASE_NOTIFICATION = 1
    }
}