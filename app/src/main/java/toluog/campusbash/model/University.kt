package toluog.campusbash.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.Keep
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 12/28/2017.
 */
@Keep
@Entity(tableName = AppContract.UNIVERSITY_TABLE)
data class University(@PrimaryKey var uniId: String = "", var name: String = "", var city: String = "",
                      var province: String = "", var country: String = "", var nickName: String = "",
                      var shortName: String = "")