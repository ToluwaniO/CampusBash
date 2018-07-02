package toluog.campusbash.data.network

import android.support.annotation.Keep

@Keep
data class EphemeralResponse(var status: Int, var message: String, var key: String)