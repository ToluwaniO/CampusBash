package toluog.campusbash.utils

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.iid.FirebaseInstanceId



class MyFirebaseInstanceIDService: FirebaseInstanceIdService() {

    private val TAG = MyFirebaseInstanceIDService::class.java.simpleName

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        if(refreshedToken != null) {
            Log.d(TAG, "FCM token refreshed")
            Util.setPrefInt(applicationContext, AppContract.PREF_FCM_TOKEN_UPDATED, 0)
        }
    }
}