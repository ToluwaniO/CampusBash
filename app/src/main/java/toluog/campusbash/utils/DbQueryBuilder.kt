package toluog.campusbash.utils

/**
 * Created by oguns on 3/17/2018.
 */
class DbQueryBuilder {
    companion object {
        fun buildEventQuery(queryMap: HashMap<String, String>): String {
            val builder = StringBuilder()
            val time = queryMap["time"]
            val type = queryMap["type"]
            val text = queryMap["text"] ?: ""
            queryMap.remove("text")

            builder.append(" eventName LIKE '$text%'")

            if(time != null) {
                builder.append(" AND startTime LIKE $time ")
            }
            if(type != null) {
                builder.append(" AND eventType LIKE $type")
            }

            return builder.toString()
        }
    }
}