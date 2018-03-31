package toluog.campusbash.data.network

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.GsonBuilder
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class StripeServerClient(private val body: StripeAccountBody) {
    private lateinit var httpClientAPI: StripeClientAPI
    private val result = MutableLiveData<ServerResponse>()
    private val TAG = StripeServerClient::class.java.simpleName
    private lateinit var response: Call<ServerResponse>

    private fun createStripeClient(): StripeClientAPI {
        val gson = GsonBuilder()
                .setLenient()
                .create()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
        return Retrofit.Builder()
                .baseUrl(CAMPUSBASH_STRIPE_SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
                .create(StripeClientAPI::class.java)
    }

    fun createStripeAccount(): MutableLiveData<ServerResponse> {
        httpClientAPI = createStripeClient()

        launch {
            response = httpClientAPI.createStripeAccount(body)
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

        return result
    }
    private fun jsonToMap(response: String): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        val root = JSONObject(response)
        map["code"] = root["code"]
        map["message"] = root["message"]
        return map
    }

    interface StripeClientAPI {
        @POST("stripe/createStripeAccount")
        fun createStripeAccount(@Body account: StripeAccountBody): Call<ServerResponse>
    }


    companion object {
        private const val CAMPUSBASH_STRIPE_SERVER_URL = "https://us-central1-campusbash-e0ca8.cloudfunctions.net/"
    }
}