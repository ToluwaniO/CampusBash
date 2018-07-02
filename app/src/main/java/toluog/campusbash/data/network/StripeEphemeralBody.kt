package toluog.campusbash.data.network

import android.support.annotation.Keep

@Keep
data class StripeEphemeralBody(val customerId: String, val apiVersion: String)