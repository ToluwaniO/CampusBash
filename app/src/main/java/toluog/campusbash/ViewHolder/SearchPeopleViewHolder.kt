package toluog.campusbash.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.profile_list_item.*
import org.jetbrains.anko.intentFor
import toluog.campusbash.model.PublicProfile
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.loadImage
import toluog.campusbash.view.ProfileActivity

class SearchPeopleViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(profile: PublicProfile) {
        profile_pic.loadImage(profile.photoUrl)
        user_name.text = profile.userName
        user_summary.text = profile.summary

        containerView.setOnClickListener {
            val context = it.context
            context.startActivity(context.intentFor<ProfileActivity>().apply {
                putExtra(AppContract.PROFILE_UID, profile.uid)
            })
        }
    }
}