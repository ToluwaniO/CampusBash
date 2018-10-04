package toluog.campusbash.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.algolia.search.saas.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import toluog.campusbash.model.PublicProfile
import org.json.JSONObject


class PeopleViewModel(app: Application): GeneralViewModel(app) {

    private val TAG = PeopleViewModel::class.java.simpleName
    private val settings = JSONObject().apply {
        put("searchableAttributes", listOf("userName"))
    }

    private val client = Client("XFIMOPYAL4", "651b0bc3f4293759d55768cd9631463a")
    private val index = client.getIndex("publicProfile").apply {
        setSettingsAsync(settings, null)
    }

    private val users = MutableLiveData<List<PublicProfile>>()

    private val completionHandler = CompletionHandler { obj, err ->
        if (obj == null) return@CompletionHandler
        val data = getProfileList(obj.getJSONArray("hits").toString())
        Log.d(TAG, "$data")
        users.postValue(data)
    }

    fun search(query: String) {
        if (query.isBlank()) {
            users.postValue(emptyList())
            return
        }
        index.searchAsync(Query(query), completionHandler)
    }

    private fun getProfileList(profiles: String):ArrayList<PublicProfile>{
        val listType = object : TypeToken<ArrayList<PublicProfile>>() {}.type
        return Gson().fromJson(profiles, listType)
    }

    fun getPeople() = users

}