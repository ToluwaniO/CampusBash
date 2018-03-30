package toluog.campusbash.data.network

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

class StripeServerClient(private val body: StripeAccountBody) {
    private lateinit var httpClientAPI: StripeClientAPI
    private val result = MutableLiveData<HashMap<String, Any>>()
    private val TAG = StripeServerClient::class.java.simpleName
    private lateinit var response: Call<String>

    private fun createStripeClient(): StripeClientAPI {
        return Retrofit.Builder()
                .baseUrl(CAMPUSBASH_SERVER_URL)
                .build()
                .create(StripeClientAPI::class.java)
    }

    fun createStripeAccount(): MutableLiveData<HashMap<String, Any>> {
        httpClientAPI = createStripeClient()
        response = httpClientAPI.createStripeAccount(body)
        response.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                if(response != null) {
                    val body = response.body()
                    Log.d(TAG, "CODE -> ${response.code()}\nMESSAGE -> ${response.message()}")
                    if(body != null){
                        result.postValue(jsonToMap(body))
                    } else {
                        Log.d(TAG, "Response body is null")
                    }
                } else {
                    Log.d(TAG, "response is null")
                }
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                Log.d(TAG, "An error occurred\ne -> ${t?.message}")
            }
        })
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
        @POST("/createStripeAccount")
        fun createStripeAccount(@Body account: StripeAccountBody): Call<String>
    }

    companion object {
        private const val CAMPUSBASH_SERVER_URL = "https://us-central1-campusbash-e0ca8.cloudfunctions.net"
    }
}