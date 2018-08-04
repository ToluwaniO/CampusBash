package toluog.campusbash.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import com.firebase.ui.auth.AuthUI
import de.hdodenhof.circleimageview.CircleImageView
import toluog.campusbash.utils.AppContract.Companion.RC_SIGN_IN
import android.preference.PreferenceManager
import android.view.inputmethod.InputMethodManager
import kotlin.collections.HashSet
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.RetryStrategy
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.Trigger
import toluog.campusbash.data.CurrencyDataSource
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.support.v4.content.ContextCompat.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.stripe.android.model.Card
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import toluog.campusbash.BuildConfig
import toluog.campusbash.view.NoNetworkActivity
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.collections.HashMap


/**
 * Created by oguns on 12/14/2017.
 */
class Util{
    companion object {

        private val TAG = Util::class.java.simpleName
        private val configProvider = ConfigProvider(FirebaseRemoteConfig.getInstance())
        private var mDispatcher: FirebaseJobDispatcher? = null
        private val shortMonthsCaps = arrayOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG",
                "SEP", "OCT", "NOV", "DEC")
        private val shortDays = arrayOf("Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat")
        private val shortMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec")
        private val utfCodes: Map<String, String> = mapOf("%21" to "!", "%22" to "\"", "%23" to "#", "%24" to "$",
                "%26" to "&", "%27" to "'", "%28" to "(", "%29" to ")", "%2A" to "*",
                "%2B" to "+", "%2C" to ",", "%2D" to "-", "%2E" to ".", "%2F" to "/", "%30" to "0",
                "%31" to "1", "%32" to "2", "%33" to "3", "%34" to "4", "%35" to "5", "%36" to "6",
                "%37" to "7", "%38" to "8", "%39" to "9", "%3A" to ":", "%3B" to ";", "%3C" to "<",
                "%3D" to "=", "%3E" to ">", "%3F" to "?", "%40" to "@", "%41" to "A", "%42" to "B",
                "%43" to "C", "%44" to "D", "%45" to "E", "%46" to "F", "%47" to "G", "%48" to "H",
                "%49" to "I", "%4A" to "J", "%4B" to "K", "%4C" to "L", "%4D" to "M", "%4E" to "N",
                "%4F" to "O", "%50" to "P", "%51" to "Q", "%52" to "R", "%53" to "S", "%54" to "T",
                "%55" to "U", "%56" to "V", "%57" to "W", "%58" to "X", "%59" to "Y", "%5A" to "Z",
                "%5B" to "[", "%5C" to "\\", "%5D" to "]", "%5E" to "^", "%5F" to "_", "%60" to "`",
                "%61" to "a", "%62" to "b", "%63" to "c", "%64" to "d", "%65" to "e", "%66" to "f",
                "%67" to "g", "%68" to "h", "%69" to "i", "%6A" to "j", "%6B" to "k", "%6C" to "l",
                "%6D" to "m", "%6E" to "n", "%6F" to "o", "%70" to "p", "%71" to "q", "%72" to "r",
                "%74" to "t", "%75" to "u", "%76" to "v", "%77" to "w", "%78" to "x", "%79" to "y",
                "%7A" to "z", "%7B" to "{", "%7C" to "|", "%7D" to "}", "%7E" to "~", "%A2" to "¢",
                "%A3" to "£", "%A5" to "¥", "%A6" to "|", "%A7" to "§", "%AB" to "«", "%AC" to "¬",
                "%AD" to "¯", "%B0" to "º", "%B1" to "±", "%B2" to "ª", "%B4" to ",", "%B5" to "µ",
                "%BB" to "»", "%BC" to "¼", "%BD" to "½", "%BF" to "¿", "%C0" to "À", "%C1" to "Á",
                "%C2" to "Â", "%C3" to "Ã", "%C4" to "Ä", "%C5" to "Å", "%C6" to "Æ", "%C7" to "Ç",
                "%C8" to "È", "%C9" to "É", "%CA" to "Ê", "%CB" to "Ë", "%CC" to "Ì", "%CD" to "Í",
                "%CE" to "Î", "%CF" to "Ï", "%D0" to "Ð", "%D1" to "Ñ", "%D2" to "Ò", "%D3" to "Ó",
                "%D4" to "Ô", "%D5" to "Õ", "%D6" to "Ö", "%D8" to "Ø", "%D9" to "Ù", "%DA" to "Ú",
                "%DB" to "Û", "%DC" to "Ü", "%DD" to "Ý", "%DE" to "Þ", "%DF" to "ß", "%E0" to "à",
                "%E1" to "á", "%E2" to "â", "%E3" to "ã", "%E4" to "ä", "%E5" to "å", "%E6" to "æ",
                "%E7" to "ç", "%E8" to "è", "%E9" to "é", "%EA" to "ê", "%EB" to "ë", "%EC" to "ì",
                "%ED" to "í", "%EE" to "î", "%EF" to "ï", "%F0" to "ð", "%F1" to "ñ", "%F2" to "ò",
                "%F3" to "ó", "%F4" to "ô", "%F5" to "õ", "%F6" to "ö", "%F7" to "÷", "%F8" to "ø",
                "%F9" to "ù", "%FA" to "ú", "%FB" to "û", "%FC" to "ü", "%FD" to "ý", "%FE" to "þ",
                "%FF" to "ÿ", "%73" to "s")//, "%" to "%25"


        fun formatDate(calendar: Calendar): String {
            val dayName = shortDays[calendar[Calendar.DAY_OF_WEEK]-1]
            val day = calendar[Calendar.DAY_OF_MONTH]
            val month = shortMonths[calendar[Calendar.MONTH]]
            val year = calendar[Calendar.YEAR]
            return "$dayName, $month $day, $year"
        }

        fun formatTime(calendar: Calendar): String {
            var hour = ""
            var minute = ""
            val hVal = calendar[Calendar.HOUR_OF_DAY]
            val mVal = calendar[Calendar.MINUTE]

            hour = if(hVal < 10) {
                "0$hVal"
            } else{
                "$hVal"
            }

            minute = if(mVal < 10) {
                "0$mVal"
            } else {
                "$mVal"
            }

            return "$hour:$minute"
        }

        fun formatDateTime(date: Date): String{
            val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
            return df.format(date)
        }

        fun formatDateTime(calendar: Calendar) = "${formatDate(calendar)} ${formatTime(calendar)}"

        fun getPeriod(start: Long, end: Long): String {
            val sCal = Calendar.getInstance()
            sCal.timeInMillis = start
            val eCal = Calendar.getInstance()
            eCal.timeInMillis = end
            val sDay = sCal[Calendar.DAY_OF_YEAR]
            val sYear = sCal[Calendar.YEAR]
            val eDay = eCal[Calendar.DAY_OF_YEAR]
            val eYear = eCal[Calendar.YEAR]
            var date = ""

            return if (sDay == eDay && sYear == eYear) {
                "${formatDate(sCal)} ${formatTime(sCal)} - ${formatTime(eCal)}"
            } else if(sDay.toInt() < eDay.toInt() && sYear == eYear) {
                val a = formatDate(sCal)
                a.removeRange(a.length-6, a.length-1)
                val b = formatDate(eCal)
                "$a ${formatTime(sCal)} - $b ${formatTime(eCal)}"
            } else {
                "${formatDateTime(sCal)} - ${formatDateTime(eCal)}"
            }
        }

        fun getShortMonth(date: Long): String{
            val cal = Calendar.getInstance()
            cal.timeInMillis = date
            val m = cal.get(Calendar.MONTH)
            return shortMonthsCaps[m]
        }

        fun getDay(date: Long): String {
            val cal = Calendar.getInstance()
            cal.timeInMillis = date
            val d = cal.get(Calendar.DAY_OF_MONTH)

            if(d < 10) return "0$d"
            return "$d"
        }

        fun hideKeyboard(ctx: Context) {
            val inputManager = ctx
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // check if no view has focus:
            val v = (ctx as Activity).currentFocus ?: return
            inputManager.hideSoftInputFromWindow(v.windowToken, 0)
        }

        fun startSignInActivity(activity: Activity){
            Log.d(TAG, "startSignInActivity called")
            val providers = Arrays.asList(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.FacebookBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build())
            activity.startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers).build(), RC_SIGN_IN)
        }

        fun getPrefInt(context: Context, key: String): Int {
            Log.d(TAG, "pref gotten")
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPref.getInt(key,0)
        }

        fun setPrefInt(context: Context, key: String, value: Int){
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPref.edit()
            editor.putInt(key, value)
            editor.apply()
            Log.d(TAG, "PREF [\"key\" : $value]")
        }

        fun getPrefString(context: Context, key: String): String {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPref.getString(key,"")
        }

        fun setPrefString(activity: Activity, key: String, value: String){
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = sharedPref.edit()
            editor.putString(key, value)
            editor.apply()
            Log.d(TAG, "PREF [\"$key\" : \"$value\"]")
        }

        fun setPrefStringSet(activity: Activity, key: String, value: Set<String>){
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = sharedPref.edit()
            editor.putStringSet(key, value)
            editor.apply()
            Log.d(TAG, "PREF [\"$key\" : $value]")
        }

        fun getPrefStringSet(activity: Activity, key: String): MutableSet<String> {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            return sharedPref.getStringSet(key,HashSet<String>())
        }

        fun downloadCurrencies(context: Context) {
            CurrencyDataSource.downloadCurrencies(FirebaseFirestore.getInstance(), context)
        }

        fun scheduleEventDeleteJob(context: Context) {
            val mDispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            val myJob = mDispatcher.newJobBuilder()
                    .setService(MyJobService::class.java)
                    .setTag(AppContract.JOB_EVENT_DELETE)
                    .setRecurring(true)
                    .setTrigger(Trigger.executionWindow(5, 30))
                    .setLifetime(Lifetime.FOREVER)
                    .setReplaceCurrent(false)
                    .setConstraints(Constraint.DEVICE_CHARGING)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .build()
            mDispatcher.mustSchedule(myJob)
        }

        fun cancelAllJobs(context: Context) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            //Cancel all the jobs for this package
            dispatcher.cancelAll()
        }

        fun fixLink(link: String): String {
            var l = link
            Log.d(TAG, "old Link -> $l")
            val keys = utfCodes.keys
            for (i in keys) {
                val replacement = utfCodes[i]
                if (replacement != null) {
                    l = l.replace(i, replacement)
                }
            }
            Log.d(TAG, "new Link -> $l")
            return l
        }

        fun dateRangeCheck(date: Long, rangeA: Long, rangeB: Long): Boolean {
            return date in rangeA..rangeB
        }

        fun getFinalFee(ticketFee: Double): HashMap<String, BigDecimal> {
            Log.d(TAG, "Ticket Fee -> (start) $ticketFee")
            val map = HashMap<String, BigDecimal>()
            var paymentFee = (configProvider.stripeTicketCut()+ configProvider.campusbashTicketCut())/100
            val serviceFee = configProvider.stripeServiceFee() + configProvider.campusbashServiceFee()
            var totalFee = ticketFee+serviceFee
            totalFee /= (1-paymentFee)
            paymentFee *= totalFee
            map[AppContract.TICKET_FEE] = if(ticketFee > 0) {
                val r = round(ticketFee, 2)
                Log.d(TAG, "Ticket Fee -> (end) $r")
                r
            } else {
                BigDecimal("0")
            }
            map[AppContract.SERVICE_FEE] = if(ticketFee > 0) {
                Log.d(TAG, "Service Fee -> (start) -> $$serviceFee")
                Log.d(TAG, "Service Fee -> (end)")
                round(serviceFee, 2)
            } else {
                BigDecimal("0")
            }
            map[AppContract.PAYMENT_FEE] = if(ticketFee > 0) {
                Log.d(TAG, "Payment Fee -> (start) -> $$paymentFee")
                Log.d(TAG, "Payment Fee -> (end)")
                round(paymentFee, 2)
            } else {
                BigDecimal("0")
            }
            map[AppContract.TOTAL_FEE] = if(ticketFee > 0) {
                Log.d(TAG, "Total Fee -> (start) -> $$totalFee")
                Log.d(TAG, "Total Fee -> (end)")
                round(totalFee, 2)
            } else {
                BigDecimal("0")
            }
            val cFee = configProvider.campusbashServiceFee() + ticketFee * configProvider.campusbashTicketCut()/100
            map[AppContract.CAMPUSBASH_FEE] = BigDecimal("${round(cFee, 2)}")
            Log.d(TAG, "Breakdown -> $map")
            return map
        }

        fun round(value: Double, places: Int): BigDecimal {
            if (places < 0) throw IllegalArgumentException()
            val bd = BigDecimal(value.toString())
            bd.setScale(places, RoundingMode.DOWN)
            var db = bd.toString()
            for (i in 0 until places) {
                db += "0"
            }
            val sides = db.split(".")
            val result = "${sides[0]}.${sides[1].subSequence(0,places)}"
            val res = BigDecimal(result)
            Log.d(TAG, "Final Fee -> $$res")
            return res
        }

        fun debugMode() = BuildConfig.DEBUG

        fun isConnected(context: Context?): Boolean {
            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val activeNetwork = cm?.activeNetworkInfo
            return activeNetwork?.isConnected ?: false
        }

        fun getObject(value: String?): JSONObject? {
            if (value == null) return null
            return JSONObject(value)
        }

        fun getCardFromJson(value: String?): Card? {
            val json = Util.getObject(value)
            val card = Card.fromJson(json)
            val number = json?.get(CARD_NUMBER) as String?
            return Card(number, card?.expMonth, card?.expYear, card?.cvc)
        }
    }
}