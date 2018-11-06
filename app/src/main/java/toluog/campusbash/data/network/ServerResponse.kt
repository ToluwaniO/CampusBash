package toluog.campusbash.data.network

import android.support.annotation.Keep

@Keep
data class ServerResponse(var status: Int, var message: String, var value: Any? = null)