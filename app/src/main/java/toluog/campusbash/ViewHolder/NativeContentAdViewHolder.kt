package toluog.campusbash.ViewHolder

import com.google.android.gms.ads.formats.NativeContentAdView
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.android.gms.ads.formats.NativeContentAd
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.native_ad_view.*


/**
 * Created by oguns on 1/28/2018.
 */
class NativeContentAdViewHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(ad: NativeContentAd) {
        contentad_headline.text = ad.headline
        contentad_body.text = ad.body
        contentad_call_to_action.text = ad.callToAction
        contentad_advertiser.text = ad.advertiser

        val images = ad.images

        if (images.size > 0) {
            contentad_image.setImageDrawable(images[0].drawable)
        }

        val logoImage = ad.logo

        if (logoImage == null) {
            contentad_logo.visibility = View.INVISIBLE
        } else {
            contentad_logo.setImageDrawable(logoImage.drawable)
        }

        adView.setNativeAd(ad)
    }
}