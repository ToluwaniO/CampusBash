package toluog.campusbash.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.profile_fragment_layout.*
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.intentFor
import toluog.campusbash.R
import toluog.campusbash.R.id.*
import toluog.campusbash.R.string.interests
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

/**
 * Created by oguns on 12/26/2017.
 */
class ProfileFragment(): Fragment() {

    private val TAG = ProfileFragment::class.java.simpleName
    private lateinit var rootView: View
    lateinit var university:String
    lateinit var country: String
    val util = Util()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.profile_fragment_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseManager.getUser()
        if(user != null) {
            Glide.with(rootView.context).load(user.photoUrl).into(profile_image)
            profile_name.text = user.displayName
            profile_email.text = user.email
        }

        university = Util.getPrefString(act, AppContract.PREF_UNIVERSITY_KEY)
        country = Util.getPrefString(act, AppContract.PREF_COUNTRY_KEY)

        profile_university.text = university
        profile_country.text = country

        update_interests_button.setOnClickListener {
            startActivity(intentFor<InterestsActivity>())
        }

        sign_out_button.setOnClickListener {
            FirebaseManager.signOut()
            Util.startSignInActivity(act)
        }

    }
}