package toluog.campusbash.view.viewmodel

import android.app.Application
import kotlinx.coroutines.launch
import toluog.campusbash.data.CloudFunctions
import toluog.campusbash.view.viewmodel.GeneralViewModel

class ProfileViewModel(app: Application): GeneralViewModel(app) {
    private val cloudFucntions = CloudFunctions()
    fun getUserProfile(uid: String) = generalRepository.getPublicProfile(uid)

    fun getFollowers(uid: String) = generalRepository.getFollowers(uid)

    fun getFollowing(uid: String) = generalRepository.getFollowing(uid)

    fun getEvents(uid: String) = generalRepository.getUserEvents(uid)

    fun getPlaces() = generalRepository.getPlaces()

    fun destroyEventListener() = generalRepository.destroyUserEventListener()

    fun followUser(uid: String) {
        this.launch {
            cloudFucntions.followUser(uid)
        }
    }

    fun unfollowUser(uid: String) {
        this.launch {
            cloudFucntions.unfollowUser(uid)
        }
    }
}