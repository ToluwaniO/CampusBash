package toluog.campusbash.data.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import org.json.JSONObject
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.stripe.android.EphemeralKeyProvider
import com.stripe.android.EphemeralKeyUpdateListener
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
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

    fun createStripeAccount(body: StripeAccountBody, uid: String): MutableLiveData<ServerResponse> {
        httpClientAPI = createStripeClient()
        FirebaseManager.getAuthToken()
                ?.addOnCompleteListener {
                    val token = it.result.token
                    if(token != null) {
                        launch {
                            handleAccountResponse(body, uid, token)
                        }
                    } else {
                        Log.d(TAG, "Could not get token")
                    }
                }
                ?.addOnFailureListener {
                    Log.d(TAG, it.message)
                }

        return result
    }

    private suspend fun handleAccountResponse(body: StripeAccountBody, uid: String, token: String) {
        try {
            response = httpClientAPI.createStripeAccount(uid, token, body)
            val sResponse = response.await()
            Log.d(TAG, "$sResponse")
            result.postValue(sResponse)
        } catch (e: HttpException) {
            Log.d(TAG, e.message())
        } catch (e: Throwable) {
            Log.d(TAG, e.message)
        }
    }

    fun createEphemeralKey(customerId: String, apiVersion: String, uid: String) {
        httpClientAPI = createStripeClient()
        FirebaseManager.getAuthToken()
                ?.addOnCompleteListener {
                    val token = it.result.token
                    if(token != null) {
                        Log.d(TAG, token)
                        launch {
                            handleEphemeralKeyResponse(customerId, apiVersion, uid, token)
                        }
                    } else {
                        Log.d(TAG, "Could not get token")
                    }
                }
                ?.addOnFailureListener {
                    Log.d(TAG, it.message)
                }
    }

    fun getEphemeralKey() = ephemeralKey

    private suspend fun handleEphemeralKeyResponse(customerId: String, apiVersion: String, uid: String, token: String) {
            try {
                val body = mapOf(
                        "customerId" to customerId,
                        "apiVersion" to apiVersion
                )
                ephemeralResponse = httpClientAPI.createEphemeralKey(uid, token, body)
                val eResponse = ephemeralResponse.await()
                Log.d(TAG, "$eResponse")
                ephemeralKey.postValue(eResponse.key)
            } catch (e: HttpException) {
                Log.d(TAG, e.message())
            } catch (e: Throwable) {
                Log.d(TAG, e.message)
            }
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