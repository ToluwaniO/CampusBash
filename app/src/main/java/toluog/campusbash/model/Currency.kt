package toluog.campusbash.model

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 3/2/2018.
 */
@Keep
@SuppressLint("ParcelCreator")
@Entity(tableName = AppContract.CURRENCY_TABLE)
@Parcelize
data class Currency(@PrimaryKey var id: String = "", var name: String = "", var namePlural: String = "",
                    var symbol: String = "", var symbolNative: String = "", var code: String = "",
                    var rounding: Int = 0, var decimalDigits: Int = 0): Parcelable