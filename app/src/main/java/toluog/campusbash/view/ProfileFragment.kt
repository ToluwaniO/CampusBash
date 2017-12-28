package toluog.campusbash.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.profile_fragment_layout.*
import toluog.campusbash.R
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

/**
 * Created by oguns on 12/26/2017.
 */
class ProfileFragment(): Fragment() {
    var rootView: View? = null
    val util = Util()
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.profile_fragment_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseManager.getUser()
        if(user != null) {
            Glide.with(rootView?.context).load(user.photoUrl).into(profile_image)
            profile_name.text = user.displayName
            profile_email.text = user.email
        }



    }
}