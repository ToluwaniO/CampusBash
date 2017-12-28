package toluog.campusbash.utils

import android.app.Activity
import android.content.Context
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import com.firebase.ui.auth.AuthUI
import de.hdodenhof.circleimageview.CircleImageView
import toluog.campusbash.utils.AppContract.Companion.RC_SIGN_IN
import android.R.id.edit
import android.content.SharedPreferences




/**
 * Created by oguns on 12/14/2017.
 */
class Util{
    companion object {

        private val TAG = Util::class.java.simpleName

        fun formatDate(calendar: Calendar) = "${calendar[Calendar.DAY_OF_MONTH]}/${calendar[Calendar.MONTH]}" +
                "/${calendar[Calendar.YEAR]}"

        fun formatTime(calendar: Calendar) = "${calendar[Calendar.HOUR_OF_DAY]} : ${calendar[Calendar.MINUTE]}"

        fun formatDateTime(date: Date): String{
            val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
            return df.format(date)
        }

        fun startSignInActivity(activity: Activity){
            Log.d(TAG, "startSignInActivity called")
            val providers = Arrays.asList(
                    AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build())
            activity.startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(providers).build(), RC_SIGN_IN)
        }

        fun getPrefInt(activity: Activity, key: String): Int {
            Log.d(TAG, "pref gotten")
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            return sharedPref.getInt(key,0)
        }

        fun setPrefInt(activity: Activity, key: String, value: Int){
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putInt(key, value)
            editor.apply()
            Log.d(TAG, "pref set")
        }

        fun getPrefString(activity: Activity, key: String): String {
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            return sharedPref.getString(key,"")
        }

        fun setPrefString(activity: Activity, key: String, value: String){
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun ImageView.loadImage(url: String, context: Context){
            Glide.with(context).load(url).into(this)
        }

        fun CircleImageView.loadImage(url: String, context: Context){
            Glide.with(context).load(url).into(this)
        }
    }

}