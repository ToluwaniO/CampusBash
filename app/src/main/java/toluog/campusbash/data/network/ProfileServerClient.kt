package toluog.campusbash.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import toluog.campusbash.data.network.ServerClientContract.Companion.APP_ENGINE_URL
import toluog.campusbash.utils.Util
import java.util.concurrent.TimeUnit
import kotlin.Exception

class ProfileServerClient: ServerClient<ProfileServerClient.ProfileClientApi> {

    private var httpClientAPI: ProfileClientApi
    private val TAG = ProfileServerClient::class.java.simpleName

    init {
        httpClientAPI = createClient()
    }

    override fun createClient(): ProfileClientApi {
        val gson = GsonBuilder()
                .setLenient()
                .create()
        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> Log.d(TAG, message) })
        logging.level = HttpLoggingInterceptor.Level.BASIC
        val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
        if(Util.debugMode()) {
            client.addInterceptor(logging)
        }
        val baseUrl = if (Util.devFlavor()) {
            ServerClientContract.APP_ENGINE_DEV_URL
        } else {
            ServerClientContract.APP_ENGINE_URL
        }
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client.build())
                .build()
                .create(ProfileClientApi::class.java)
    }

    suspend fun isNewStudentId(uid: String, studentId: String): ServerResponseState {
        return try {
            val isNew = httpClientAPI.isNewStudentId(uid, studentId).await()
            Log.d(TAG, "$isNew")
            ServerResponseState.Success(isNew.new)
        } catch (e: Exception) {
            Log.d(TAG, e.message)
            ServerResponseState.Error(e)
        }
    }

    interface ProfileClientApi {
        @GET("isNewStudentId")
        fun isNewStudentId(@Query("uid") uid: String, @Query("studentId") studentId: String): Deferred<IsNewStudentIdResponse>
    }
}