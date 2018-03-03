package toluog.campusbash.model

import android.annotation.SuppressLint
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 3/2/2018.
 */
@SuppressLint("ParcelCreator")
@Entity(tableName = AppContract.CURRENCY_TABLE)
@Parcelize
data class Currency(@PrimaryKey var id: String = "", var name: String = "", var namePlural: String = "",
                    var symbol: String = "", var symbolNative: String = "", var code: String = "",
                    var rounding: Int = 0, var decimalDigits: Int = 0): Parcelable