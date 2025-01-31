package toluog.campusbash.data.network

import android.util.Log
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.*
import retrofit2.http.*
import toluog.campusbash.model.TicketPriceBreakdown
import toluog.campusbash.utils.Util
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class StripeServerClient {

    private var httpClientAPI: StripeClientAPI
    private val TAG = StripeServerClient::class.java.simpleName
    private lateinit var response: Deferred<ServerResponse>

    init {
        httpClientAPI = createStripeClient()
    }

    private fun createStripeClient(): StripeClientAPI {
        val gson = GsonBuilder()
                .setLenient()
                .create()
        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> Log.d(TAG, message) })
        logging.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().apply {
            connectTimeout(2, TimeUnit.MINUTES)
            readTimeout(2, TimeUnit.MINUTES)
            writeTimeout(2, TimeUnit.MINUTES)
        }
        if(Util.debugMode()) {
            client.addInterceptor(logging)
        }
        val url = if(Util.devFlavor()) {
            ServerClientContract.APP_ENGINE_DEV_URL
        } else {
            ServerClientContract.APP_ENGINE_URL
        }
        return Retrofit.Builder()
                .baseUrl(url)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client.build())
                .build()
                .create(StripeClientAPI::class.java)
    }

    suspend fun createStripeAccount(body: StripeAccountBody): ServerResponse {
        httpClientAPI = createStripeClient()
         return handleAccountResponse(body)
    }

    private suspend fun handleAccountResponse(body: StripeAccountBody): ServerResponse {
        try {
            response = httpClientAPI.createStripeAccount(body)
            val sResponse = response.await()
            Log.d(TAG, "$sResponse")
            return sResponse
        } catch (e: HttpException) {
            Log.d(TAG, e.message())
        } catch (e: SocketTimeoutException) {
            Log.d(TAG, e.message)
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return ServerResponse(400, "An error occurred")
    }

    suspend fun createEphemeralKey(customerId: String, apiVersion: String): String {
        httpClientAPI = createStripeClient()
        return handleEphemeralKeyResponse(customerId, apiVersion)
    }

    suspend fun getTicketBreakdown(total: Int): TicketPriceBreakdown? {
        return try {
            val breakdown = httpClientAPI.getTicketBreakdown(total).await()
            Log.d(TAG, "$breakdown")
            breakdown
        } catch (e: Exception) {
            Log.d(TAG, e.message)
            null
        }
    }

    private suspend fun handleEphemeralKeyResponse(customerId: String, apiVersion: String): String {
        val body = mapOf(
                "customerId" to customerId,
                "apiVersion" to apiVersion
        )
        val ephemeralResponse = httpClientAPI.createEphemeralKey(body).await()
        if (ephemeralResponse.status != 200) throw Exception(ephemeralResponse.message)
        Log.d(TAG, "$ephemeralResponse")
        return ephemeralResponse.value as String? ?: JSONObject().toString()
    }

    interface StripeClientAPI {
        @POST("createStripeAccount")
        fun createStripeAccount(@Body account: StripeAccountBody): Deferred<ServerResponse>

        @POST("createEphemeralKey")
        fun createEphemeralKey(@Body options: Map<String, String>): Deferred<ServerResponse>

        @GET("ticketBreakDown")
        fun getTicketBreakdown(@Query("price") price: Int): Deferred<TicketPriceBreakdown>
    }
}