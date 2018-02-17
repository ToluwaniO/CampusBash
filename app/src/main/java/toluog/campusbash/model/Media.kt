package toluog.campusbash.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by oguns on 2/13/2018.
 */

@SuppressLint("ParcelCreator")
@Parcelize
data class Media(var url: String = "", var path: String = "", var type: String = "image"): Parcelable