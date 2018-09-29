package toluog.campusbash.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.profile_list_item.*
import toluog.campusbash.model.PublicProfile
import toluog.campusbash.utils.loadImage

class SearchPeopleViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(profile: PublicProfile) {
        profile_pic.loadImage(profile.photoUrl)
        user_name.text = profile.userName
        user_summary.text = profile.summary
    }
}