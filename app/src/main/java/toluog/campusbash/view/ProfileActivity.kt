package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import toluog.campusbash.R
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.data.CloudFunctions
import toluog.campusbash.model.Event
import toluog.campusbash.model.PublicProfile
import toluog.campusbash.utils.Analytics
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.loadImage
import toluog.campusbash.viewmodel.ProfileViewModel

class ProfileActivity : AppCompatActivity(), EventAdapter.OnItemClickListener {

    private val TAG = ProfileActivity::class.java.simpleName

    private lateinit var myUid: String
    private val followers = hashSetOf<String>()
    private val following = hashSetOf<String>()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var adapter: EventAdapter

    private var profile: PublicProfile? = null

    private val cloudFunctions = CloudFunctions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        val uid = intent?.extras?.getString(AppContract.PROFILE_UID)
        if (uid == null) {
            toast(R.string.error_occurred)
            finish()
        }
        myUid = FirebaseManager.getUser()?.uid ?: ""

        initView(uid ?: "")

        viewModel.getUserProfile(uid ?: "").observe(this, Observer {
            Log.d(TAG, "$it")
            profile = it
            if (it != null) {
                updateProfileSection(it)
            }
        })

        viewModel.getEvents(uid ?: "").observe(this, Observer {
            Log.d(TAG, "$it")
            if (it != null) {
                adapter.notifyListChanged(ArrayList<Any>().apply {
                    addAll(it)
                })
            } else {
                adapter.notifyListChanged(arrayListOf())
            }
        })

        viewModel.getPlaces()?.observe(this, Observer {
            if (it != null) {
                adapter.notifyPlaceChanged(it)
            }
        })

        viewModel.getFollowers(uid ?: "").observe(this, Observer {
            followers.clear()
            if (it != null) {
                val uids = it.map { it.uid }
                followers.addAll(uids)
            }
            Log.d(TAG, "$followers")
            val p = profile
            if (p != null) updateProfileSection(p)
        })

        viewModel.getFollowing(uid ?: "").observe(this, Observer {
            following.clear()
            if (it != null) {
                val uids = it.map { it.uid }
                following.addAll(uids)
            }
            Log.d(TAG, "$following")
            val p = profile
            if (p != null) updateProfileSection(p)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onDestroy() {
        viewModel.destroyEventListener()
        super.onDestroy()
    }

    override fun onItemClick(event: Event, view: View) {
        Analytics.logEventSelected(event)
        val bundle = Bundle()
        bundle.putString(AppContract.MY_EVENT_BUNDLE, event.eventId)
        startActivity(intentFor<ViewEventActivity>().putExtras(bundle))
    }

    private fun updateProfileSection(profile: PublicProfile) {
        profile_pic.loadImage(profile.photoUrl)
        user_name.text = profile.userName
        followers_count.text = "${profile.followers}"
        following_count.text = "${profile.following}"
        profile_summary.text = profile.summary

        if (profile.uid == myUid) {
            action_button.text = getString(R.string.edit_profile)
        } else if (!following.contains(myUid) && !followers.contains(myUid)) {
            action_button.text = getString(R.string.follow)
        } else if (following.contains(myUid) && !followers.contains(myUid)) {
            action_button.text = getString(R.string.follow_back)
        } else {
            action_button.text = getString(R.string.unfollow)
        }
    }

    private fun initView(uid: String) {
        adapter = EventAdapter(arrayListOf(), emptyList(), this)
        event_recycler.layoutManager = LinearLayoutManager(this)
        event_recycler.adapter = adapter

        action_button.setOnClickListener {
            val p = profile ?: return@setOnClickListener
            val action = getProfileAction(p)
            handleAction(action, uid)
        }
    }

    private fun handleAction(action: ProfileAction, uid: String) {
        when (action) {
            ProfileAction.FOLLOW, ProfileAction.FOLLOW_BACK -> {
                followers.add(myUid)
                profile?.followers = (profile?.followers ?: 0)+1
                profile?.let { updateProfileSection(it) }
                cloudFunctions.followUser(uid)
            }
            ProfileAction.MY_PROFILE -> startActivity<SetupProfileActivity>()
            ProfileAction.UNFOLLOW -> {
                followers.remove(myUid)
                profile?.followers = (profile?.followers ?: 1)-1
                profile?.let { updateProfileSection(it) }
                cloudFunctions.unfollowUser(uid)
            }
        }
    }

    private fun getProfileAction(profile: PublicProfile): ProfileAction {
        if (profile.uid == myUid) {
            return ProfileAction.MY_PROFILE
        } else if (!following.contains(myUid) && !followers.contains(myUid)) {
            return ProfileAction.FOLLOW
        } else if (following.contains(myUid) && !followers.contains(myUid)) {
            return ProfileAction.FOLLOW_BACK
        }
        return ProfileAction.UNFOLLOW
    }

}

enum class ProfileAction {
    MY_PROFILE, FOLLOW, UNFOLLOW, FOLLOW_BACK
}
