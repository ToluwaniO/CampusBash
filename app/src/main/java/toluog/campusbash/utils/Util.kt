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
import toluog.campusbash.data.Repository


/**
 * Created by oguns on 12/14/2017.
 */
class Util{
    companion object {

        private val TAG = Util::class.java.simpleName
        private var mDispatcher: FirebaseJobDispatcher? = null
        private val shortMonthsCaps = arrayOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG",
                "SEP", "OCT", "NOV", "DEC")
        private val shortDays = arrayOf("Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat")
        private val shortMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec")

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
                    AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build())
            activity.startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers).build(), RC_SIGN_IN)
        }

        fun getPrefInt(activity: Activity, key: String): Int {
            Log.d(TAG, "pref gotten")
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            return sharedPref.getInt(key,0)
        }

        fun setPrefInt(activity: Activity, key: String, value: Int){
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = sharedPref.edit()
            editor.putInt(key, value)
            editor.commit()
            Log.d(TAG, "PREF [\"key\" : $value]")
        }

        fun getPrefString(activity: Activity, key: String): String {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            return sharedPref.getString(key,"")
        }

        fun setPrefString(activity: Activity, key: String, value: String){
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = sharedPref.edit()
            editor.putString(key, value)
            editor.commit()
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
            CurrencyDataSource.downloadCurrencies(context)
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

        fun ImageView.loadImage(url: String, context: Context){
            Glide.with(context).load(url).into(this)
        }

        fun CircleImageView.loadImage(url: String, context: Context){
            Glide.with(context).load(url).into(this)
        }
    }

}