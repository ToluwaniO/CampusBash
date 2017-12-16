package toluog.campusbash.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by oguns on 12/14/2017.
 */
class Util{
    companion object {
        fun formatDate(calendar: Calendar) = "${calendar[Calendar.DAY_OF_MONTH]}/${calendar[Calendar.MONTH]}" +
                "/${calendar[Calendar.YEAR]}"

        fun formatTime(calendar: Calendar) = "${calendar[Calendar.HOUR_OF_DAY]} : ${calendar[Calendar.MINUTE]}"

        fun formatDateTime(date: Date): String{
            val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
            return df.format(date)
        }
    }
}