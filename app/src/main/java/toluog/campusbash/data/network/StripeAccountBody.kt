package toluog.campusbash.data.network

import androidx.annotation.Keep
import toluog.campusbash.BuildConfig

@Keep
data class StripeAccountBody(val uid: String, val email: String, val country: String,
                                  val debug: Boolean = BuildConfig.DEBUG)