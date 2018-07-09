package toluog.campusbash.view

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.view.*
import kotlinx.android.synthetic.main.profile_fragment_layout.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.intentFor
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.loadImage

/**
 * Created by oguns on 12/26/2017.
 */
class ProfileFragment(): Fragment() {

    private val TAG = ProfileFragment::class.java.simpleName
    private lateinit var rootView: View
    lateinit var university:String
    lateinit var country: String
    private lateinit var viewModel: ProfileViewModel
    private var profileInfo: LiveData<Map<String, Any>>? = null
    val util = Util()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.profile_fragment_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent.isNestedScrollingEnabled = false
        ViewCompat.setNestedScrollingEnabled(parent, false)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        val user = FirebaseManager.getUser()
        if(user != null) {
            profileInfo = viewModel.getProfileInfo(user)
            profile_name.text = user.displayName
            profile_email.text = user.email
        }

        profileInfo?.observe(this, Observer {
            it?.let {
                val profileImageUrl = it[AppContract.FIREBASE_USER_PHOTO_URL] as String?
                profileImageUrl?.let { profile_image.loadImage(it) }
                if(profileImageUrl == null) {
                    profile_image.setImageResource(R.drawable.adult_emoji)
                } else {
                    profile_image.loadImage(profileImageUrl)
                }
                profile_summary.text = it[AppContract.FIREBASE_USER_SUMMARY] as String?
            }
        })

        university = Util.getPrefString(act, AppContract.PREF_UNIVERSITY_KEY)
        country = Util.getPrefString(act, AppContract.PREF_COUNTRY_KEY)

        university_country_view.text = "$country · $university"

        get_lit_button.setOnClickListener {
            startActivity(intentFor<CreateEventActivity>())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.profile_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        when(id) {
            R.id.menu_edit_interests -> {
                startActivity(intentFor<InterestsActivity>())
            }

            R.id.menu_edit_profile -> {
                startActivity(intentFor<SetupProfileActivity>())
                act.finish()
            }

            R.id.menu_logout -> {
                FirebaseManager.signOut()
                Util.startSignInActivity(act)
            }
        }
        return true
    }
}