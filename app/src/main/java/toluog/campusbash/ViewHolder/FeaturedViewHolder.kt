package toluog.campusbash.ViewHolder

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.featured_events_layout.*
import toluog.campusbash.adapters.EventAdapter
import toluog.campusbash.model.Featured
import toluog.campusbash.model.Place

class FeaturedViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
        LayoutContainer {
    fun bind(feature: Featured, places: List<Place>, context: Context?, adapter: EventAdapter?) {
        featured_title.text = feature.title
        if(featured_recycler.layoutManager == null) {
            featured_recycler.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        }
        featured_recycler.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        if(featured_recycler.adapter == null) {
            featured_recycler.adapter = adapter
        }
        adapter?.notifyListChanged(feature.events)
        adapter?.notifyPlaceChanged(places)
    }
}