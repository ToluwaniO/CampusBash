package toluog.campusbash.data.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.stripe.android.EphemeralKeyProvider
import com.stripe.android.EphemeralKeyUpdateListener
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.*
import toluog.campusbash.BuildConfig
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

class StripeServerClient {

    private lateinit var httpClientAPI: StripeClientAPI
    private val result = MutableLiveData<ServerResponse>()
    private val ephemeralKey = MutableLiveData<String>()
    private val TAG = StripeServerClient::class.java.simpleName
    private lateinit var ephemeralResponse: Call<EphemeralResponse>
    private lateinit var response: Call<ServerResponse>

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
                        handleAccountResponse(body, uid, token)
                    } else {
                        Log.d(TAG, "Could not get token")
                    }
                }
                ?.addOnFailureListener {
                    Log.d(TAG, it.message)
                }

        return result
    }

    private fun handleAccountResponse(body: StripeAccountBody, uid: String, token: String) {
        launch {

            response = httpClientAPI.createStripeAccount(uid, token, body)
            response.enqueue(object : Callback<ServerResponse> {
                override fun onResponse(call: Call<ServerResponse>?, response: Response<ServerResponse>?) {
                    if (response == null) {
                        Log.d(TAG, "Response is null")
                    } else {
                        Log.d(TAG, "Response -> ${response.body()}")
                    }
                    result.postValue(response?.body())
                }

                override fun onFailure(call: Call<ServerResponse>?, t: Throwable?) {
                    Log.d(TAG, "An error occurred\ne -> ${t?.message}")
                    call?.cancel()
                }
            })
        }
    }

    fun createEphemeralKey(customerId: String, apiVersion: String, uid: String) {
        httpClientAPI = createStripeClient()
        FirebaseManager.getAuthToken()
                ?.addOnCompleteListener {
                    val token = it.result.token
                    if(token != null) {
                        Log.d(TAG, token)
                        handleEphemeralKeyResponse(customerId, apiVersion, uid, token)
                    } else {
                        Log.d(TAG, "Could not get token")
                    }
                }
                ?.addOnFailureListener {
                    Log.d(TAG, it.message)
                }
    }

    fun getEphemeralKey() = ephemeralKey

    private fun handleEphemeralKeyResponse(customerId: String, apiVersion: String, uid: String, token: String) {
        launch {
            val body = mapOf(
                    "customerId" to customerId,
                    "apiVersion" to apiVersion
            )
            ephemeralResponse = httpClientAPI.createEphemeralKey(uid, token, body)
            ephemeralResponse.enqueue(object : Callback<EphemeralResponse> {
                override fun onResponse(call: Call<EphemeralResponse>?, response: Response<EphemeralResponse>?) {
                    if (response == null) {
                        Log.d(TAG, "Response is null")
                    } else {
                        Log.d(TAG, "Response -> ${response.body()}")
                    }
                    ephemeralKey.postValue(response?.body()?.key)
                }

                override fun onFailure(call: Call<EphemeralResponse>?, t: Throwable?) {
                    Log.d(TAG, "An error occurred\ne -> ${t?.message}")
                    call?.cancel()
                }
            })
        }
    }

    interface StripeClientAPI {
        @POST("stripe/{uid}/{token}/createStripeAccount")
        fun createStripeAccount(@Path("uid") uid: String, @Path("token") token: String,
                                @Body account: StripeAccountBody): Call<ServerResponse>

        @GET("stripe/{uid}/{token}/createEphemeralKey")
        fun createEphemeralKey(@Path("uid") uid: String, @Path("token") token: String,
                               @QueryMap options: Map<String, String>)
                : Call<EphemeralResponse>
    }


    companion object {
        private const val CAMPUSBASH_STRIPE_SERVER_URL = "https://us-central1-campusbash-e0ca8.cloudfunctions.net/"
        private const val CAMPUSBASH_STRIPE_SERVER_URL_DEV = "https://us-central1-campusbash-dev.cloudfunctions.net/"
    }
}