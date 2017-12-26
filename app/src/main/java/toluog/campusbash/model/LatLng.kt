package toluog.campusbash.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize

/**
 * Created by oguns on 12/2/2017.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class LatLng(var lat: Double = 0.0, var lon: Double = 0.0) : Parcelable