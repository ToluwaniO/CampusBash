package toluog.campusbash.data.network

import androidx.annotation.Keep

@Keep
data class ServerResponse(var status: Int, var message: String, var value: Any? = null)