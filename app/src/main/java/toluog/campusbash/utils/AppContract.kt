package toluog.campusbash.utils

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
                "elit volutpat nec. Orci varius natoque penatibus et magnis dis parturient montes, " +
                "nascetur ridiculus mus. Suspendisse cursus quam eget nisi tempor dapibus. Nunc sollicitudin" +
                " a enim nec ornare. Ut tincidunt suscipit lectus, vitae auctor lacus pharetra ac. " +
                "Suspendisse fermentum scelerisque bibendum."
        val STANTON_ADDRESS = "9A7, 100 University Private, Ottawa, ON K1N 6N5"
        val STANTON_COORD = LatLng(45.4221, 75.6848)
        val CREATOR = Creator("Toluwani Ogunsanya", "frfgbruf", "ifjihjri")
        val TICKETS = arrayOf(Ticket("VIP", "Want best service? You're at the right place",
                1, 10, 15.50, 0, 33L, 0, 44L))

        //FRAGMENT TAGS
        val CREATE_EVENT_TAG = "createevent"

        //ROOM KEYS
        const val EVENT_TABLE = "Events"
        const val UNIVERSITY_TABLE = "Universities"

        //FIREBASE KEYS
        val FIREBASE_EVENTS = "events"
        val FIREBASE_UNIVERSITIES = "universities"
        val FIREBASESTORAGE_EVENT_IMAGE_PLACEHOLDERS = "event_placeholder_images"

        //SHAREDPREFERENCES
        val PREF_FIRST_OPEN_KEY = "first_open"
        val PREF_COUNTRY_KEY = "pref_country"
        val PREF_UNIVERSITY_KEY = "pref_university"
        val PREF_EVENT_TYPES_KEY = "pref_event_type_set"

        //ADS
        const val NUM_EVENTS_FRAGMENT_ADS = 5
        const val MAX_EVENTS_FOR_ADS_IN_FRAGMENT = 20

        //RemoteConfig
        val configRefreshTime = 3600L

        //API
        val MAPS_API_KEY = "AIzaSyC6ArHoBH007ZRKp9CBFA8P58UJbbSsP4c"

    }
}