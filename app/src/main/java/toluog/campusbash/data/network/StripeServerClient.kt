package toluog.campusbash.data.network

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.http.*
import toluog.campusbash.BuildConfig
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

class StripeServerClient {

    private lateinit var httpClientAPI: StripeClientAPI
    private val result = MutableLiveData<ServerResponse>()
    private val ephemeralKey = MutableLiveData<String>()
    private val TAG = StripeServerClient::class.java.simpleName
    private lateinit var ephemeralResponse: Deferred<EphemeralResponse>
    private lateinit var response: Deferred<ServerResponse>

    private fun createStripeClient(): StripeClientAPI {
        val gson = GsonBuilder()
                .setLenient()
                .create()
        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> Log.d(TAG, message) })
        logging.level = HttpLoggingInterceptor.Level.BASIC
        val client = OkHttpClient.Builder()
        if(Util.debugMode()) {
            client.addInterceptor(logging)
        }
        val url = if(BuildConfig.FLAVOR.equals("dev")) {
            CAMPUSBASH_STRIPE_SERVER_URL_DEV
        } else {
            CAMPUSBASH_STRIPE_SERVER_URL
        }
        return Retrofit.Builder()
                .baseUrl(url)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client.build())
                .build()
                .create(StripeClientAPI::class.java)
    }

    suspend fun createStripeAccount(body: StripeAccountBody, uid: String): ServerResponse {
        httpClientAPI = createStripeClient()
        val token = FirebaseManager.getAuthToken()
        return handleAccountResponse(body, uid, token)
    }

    private suspend fun handleAccountResponse(body: StripeAccountBody, uid: String, token: String): ServerResponse {
        try {
            response = httpClientAPI.createStripeAccount(uid, token, body)
            val sResponse = response.await()
            Log.d(TAG, "$sResponse")
            return sResponse
        } catch (e: HttpException) {
            Log.d(TAG, e.message())
        } catch (e: Throwable) {
            Log.d(TAG, e.message)
        }
        return ServerResponse(400, "An error occurred")
    }

    suspend fun createEphemeralKey(customerId: String, apiVersion: String, uid: String): String {
        httpClientAPI = createStripeClient()
        val token = FirebaseManager.getAuthToken()
        return handleEphemeralKeyResponse(customerId, apiVersion, uid, token)
    }

    fun getEphemeralKey() = ephemeralKey

    private suspend fun handleEphemeralKeyResponse(customerId: String, apiVersion: String, uid: String, token: String): String {
        val body = mapOf(
                "customerId" to customerId,
                "apiVersion" to apiVersion
        )
        ephemeralResponse = httpClientAPI.createEphemeralKey(uid, token, body)
        val eResponse = ephemeralResponse.await()
        Log.d(TAG, "$eResponse")
        return eResponse.key
    }

    interface StripeClientAPI {
        @POST("stripe/{uid}/{token}/createStripeAccount")
        fun createStripeAccount(@Path("uid") uid: String, @Path("token") token: String,
                                @Body account: StripeAccountBody): Deferred<ServerResponse>

        @GET("stripe/{uid}/{token}/createEphemeralKey")
        fun createEphemeralKey(@Path("uid") uid: String, @Path("token") token: String,
                               @QueryMap options: Map<String, String>): Deferred<EphemeralResponse>
    }

    companion object {
        private const val CAMPUSBASH_STRIPE_SERVER_URL = "https://us-central1-campusbash-e0ca8.cloudfunctions.net/"
        private const val CAMPUSBASH_STRIPE_SERVER_URL_DEV = "https://us-central1-campusbash-dev.cloudfunctions.net/"
    }
}