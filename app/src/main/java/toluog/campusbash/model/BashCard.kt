package toluog.campusbash.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.Keep
import com.stripe.android.model.Card
import com.stripe.android.model.CustomerSource
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.convertToJson

@Keep
data class BashCard(var customerSource: CustomerSource?, var card: Card? = null, var newCard: Boolean = false) : Parcelable {
    constructor(source: Parcel) : this(
            CustomerSource.fromJson(Util.getObject(source.readString())),
            Util.getCardFromJson(source.readString()),
            1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(customerSource?.toJson()?.toString())
        writeString(card?.convertToJson()?.toString())
        writeInt((if (newCard) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BashCard> = object : Parcelable.Creator<BashCard> {
            override fun createFromParcel(source: Parcel): BashCard = BashCard(source)
            override fun newArray(size: Int): Array<BashCard?> = arrayOfNulls(size)
        }
    }
}