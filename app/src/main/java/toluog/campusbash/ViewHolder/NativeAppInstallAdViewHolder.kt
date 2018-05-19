package toluog.campusbash.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.android.gms.ads.formats.NativeAppInstallAd
import com.google.android.gms.ads.formats.NativeAppInstallAdView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.native_app_install_view.*


/**
 * Created by oguns on 1/28/2018.
 */
class NativeAppInstallAdViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(ad: NativeAppInstallAd) {
        appinstall_app_icon.setImageDrawable(ad.icon.drawable)
        appinstall_headline.text = ad.headline
        appinstall_body.text = ad.body
        appinstall_call_to_action.text = ad.callToAction

        val images = ad.images

        if(images != null && images.isNotEmpty()) {
            appinstall_image.setImageDrawable(images[0].drawable)
        }

        if(ad.price == null) {
            appinstall_price.visibility = View.INVISIBLE
        } else {
            appinstall_price.visibility = View.VISIBLE
            appinstall_price.text = ad.price
        }

        if(ad.store == null) {
            appinstall_store.visibility = View.INVISIBLE
        } else {
            appinstall_store.visibility = View.VISIBLE
            appinstall_store.text = ad.store
        }

        if(ad.starRating == null) {
            appinstall_stars.visibility = View.INVISIBLE
        } else {
            appinstall_stars.visibility = View.VISIBLE
            appinstall_stars.rating = ad.starRating.toFloat()
        }

        adView?.setNativeAd(ad)
    }
}