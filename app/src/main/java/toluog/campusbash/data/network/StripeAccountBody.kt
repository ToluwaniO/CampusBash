package toluog.campusbash.data.network

import toluog.campusbash.BuildConfig

data class StripeAccountBody(val uid: String, val email: String, val country: String,
                             val debug: Boolean = BuildConfig.DEBUG)