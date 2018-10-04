package toluog.campusbash.viewmodel

import android.app.Application
import toluog.campusbash.data.EventDataSource

class ProfileViewModel(app: Application): GeneralViewModel(app) {

    fun getUserProfile(uid: String) = repo.getPublicProfile(uid)

    fun getFollowers(uid: String) = repo.getFollowers(uid)

    fun getFollowing(uid: String) = repo.getFollowing(uid)

    fun getEvents(uid: String) = repo.getUserEvents(uid)

    fun getPlaces() = repo.getPlaces()

    fun destroyEventListener() = EventDataSource.destroyUserEventListener()
}