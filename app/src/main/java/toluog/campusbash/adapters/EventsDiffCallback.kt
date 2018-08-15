package toluog.campusbash.adapters

import android.support.v7.util.DiffUtil
import com.google.android.gms.ads.formats.NativeAppInstallAd
import com.google.android.gms.ads.formats.NativeContentAd
import toluog.campusbash.model.Event
import toluog.campusbash.model.Featured
import toluog.campusbash.model.Place

class EventsDiffCallback(private val oldList: List<Any>, private val newList: List<Any>,
                         private val lastFeaturedList: ArrayList<Any> = arrayListOf()): DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return when {
            old is Event && new is Event -> old.eventId == new.eventId
            old is NativeAppInstallAd && new is NativeAppInstallAd -> {
                old.headline == new.headline && old.body == new.body && old.icon == new.icon
                && old.price == new.price && old.starRating == new.starRating && old.images[0] == new.images[0]
                && old.callToAction == new.callToAction && old.store == new.store
            }
            old is NativeContentAd && new is NativeContentAd -> {
                old.headline == new.headline && old.body == new.body && old.images[0] == new.images[0]
                        && old.callToAction == new.callToAction
            }
            old is Featured && new is Featured -> {
                old == new
            }
            else -> false
        }
    }

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return when {
            old is Event && new is Event -> {
                return old.placeholderImage?.url == new.placeholderImage?.url &&
                        old.eventName == new.eventName && old.startTime == new.startTime &&
                        old.address == new.address
            }
            old is NativeAppInstallAd && new is NativeAppInstallAd -> old.headline == new.headline &&
                    old.body == new.body
            old is NativeContentAd && new is NativeContentAd -> old.headline == new.headline &&
                    old.body == new.body
            old is Featured && new is Featured -> lastFeaturedList.deepEquals(new.events)
            else -> false
        }
    }

    private fun List<Any>.deepEquals(other: List<Any>): Boolean {
        if(this.size != other.size) return false
        for (i in 0 until this.size) {
            val a = get(i)
            val b = other[i]
            if(a is Event && b is Event && !a.deepEquals(b)) return false
            if(a != b) return false
        }
        return true
    }

}