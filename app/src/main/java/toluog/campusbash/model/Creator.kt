package toluog.campusbash.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by oguns on 12/2/2017.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class Creator(var name: String = "", var imageUrl: String = "", var uid: String = "",
                   var stripeAccountId: String? = null) : Parcelable
