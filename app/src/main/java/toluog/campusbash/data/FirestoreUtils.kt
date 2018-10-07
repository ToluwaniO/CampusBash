package toluog.campusbash.data

enum class FirestoreQueryType {
    ARRAY_CONTAINS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQUAL_TO,
    LESS_THAN_EQUAL_TO,
    EQUAL_TO,
}

data class FirestoreQuery(var key: String = "", var value: Any? = null,
                          var queryType: FirestoreQueryType = FirestoreQueryType.EQUAL_TO)