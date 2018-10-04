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

        //FRAGMENT TAGS
        val CREATE_EVENT_TAG = "createevent"
        val CREATE_TICKETS_TAG = "view_tickets_fragment"
        val TICKETS_KEY = "tickets"

        //BUNDLE TAGS
        const val TOKEN_ID = "tokenId"
        const val NEW_CARD = "newCard"
        const val MORE_TEXT = "moreText"
        const val BOUGHT_TICKET = "bought_ticket"
        const val EVENT_ID = "eventId"
        const val WEB_VIEW_URL = "web_view_url"
        const val IMAGE_SRC = "imageSrc"
        const val IMAGE_NAME = "imageName"
        const val PROFILE_UID = "profileUid"
        const val UNIVERSITIES = "universities"

        //ROOM KEYS
        const val EVENT_TABLE = "Events"
        const val UNIVERSITY_TABLE = "Universities"
        const val CURRENCY_TABLE = "Currencies"
        const val PLACE_TABLE = "Places"

        //FIREBASE KEYS
        val FIREBASE_EVENTS = "events"
        val FIREBASE_USER_TICKETS = "userTickets"
        val FIREBASE_USER_USERNAME = "userName"
        val FIREBASE_USER_UID = "uid"
        val FIREBASE_USER_STUDENT_ID = "studentId"
        val FIREBASE_USER_PREFERENCES = "preference"
        val FIREBASE_USER_UNIVERSITY = "university"
        val FIREBASE_USER_COUNTRY = "country"
        val FIREBASE_USER_SUMMARY = "summary"
        val FIREBASE_USER_PHOTO_URL = "photoUrl"
        val FIREBASE_USER_ACCOUNT_FLAGS = "accountFlags"
        val FIREBASE_UNIVERSITIES = "universities"
        val FIREBASESTORAGE_EVENT_IMAGE_PLACEHOLDERS = "event_placeholder_images"
        val FIREBASESTORAGE_PROFILE_PHOTOS = "user_profile_photos"
        val FIREBASE_CURRENCIES = "currencies"
        val FIREBASE_USERS = "users"
        val FIREBASE_FCM_TOKEN = "fcmToken"
        val FIREBASE_EVENT_TICKET = "tickets"

        //SHAREDPREFERENCES
        const val PREF_FIRST_OPEN_KEY = "first_open"
        const val PREF_COUNTRY_KEY = "pref_country"
        const val PREF_UNIVERSITY_KEY = "pref_university"
        const val PREF_EVENT_TYPES_KEY = "pref_event_type_set"
        const val PREF_FCM_TOKEN_UPDATED = "pref_fcm_token_updated"
        const val PREF_FIRST_PLACE_ALARM = "pref_first_place_alarm"

        //ADS
        const val NUM_EVENTS_FRAGMENT_ADS = 5
        const val MAX_EVENTS_FOR_ADS_IN_FRAGMENT = 20

        //RemoteConfig
        val configRefreshTime = 3600000L
        get() {
            if(BuildConfig.DEBUG) return 0L
            return field
        }

        val PLACE_AUTOCOMPLETE_REQUEST_CODE = 48547

        //JOB TAGS
        val JOB_EVENT_DELETE = "delete_old_events"
        val ALARM_DELETE_PLACES = "alarm_delete_places"

        val STRIPE_PUBLISHABLE_KEY = if(BuildConfig.FLAVOR.equals("dev")) {
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

        //PURCHASE KEYS
        const val STRIPE_ACCOUNT_ID = "stripeAccountId"
        const val TYPE_PAID = "paid"
        const val TYPE_FREE = "free"
        const val BUYER_ID = "buyerId"
        const val BUYER_EMAIL = "buyerEmail"
        const val BUYER_NAME = "buyerName"
        const val STRIPE_CUSTOMER_ID = "stripeCustomerId"
        const val FCM_TOKEN = "fcmToken"
        const val TOKEN = "token"
        const val QUANTITY = "quantity"
        const val BREAKDOWN = "breakdown"
        const val TOTAL = "total"
        const val TICKETS = "tickets"
        const val CURRENCY = "currency"
        const val DEBUG = "debug"
        const val TIME_SPENT = "timeSpent"

        const val FIRE_EMOJI = "\uD83D\uDD25"

        val EVENT_VISIBILITY_LIST = listOf("Everyone", "Friends", "Friends of friends")
    }
}