package toluog.campusbash.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query

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

class FirestoreUtils {
    companion object {
        fun buildQuery(ref: CollectionReference, querySet: Set<FirestoreQuery>): Query {
            var qr: Query = ref
            for (q in querySet) {
                qr = addQuery(qr, q)
            }
            return qr
        }

        private fun addQuery(ref: Query, query: FirestoreQuery): Query {
            return when (query.queryType) {
                FirestoreQueryType.EQUAL_TO -> ref.whereEqualTo(query.key, query.value)
                FirestoreQueryType.GREATER_THAN -> ref.whereGreaterThan(query.key, query.value ?: Any())
                FirestoreQueryType.LESS_THAN -> ref.whereLessThan(query.key, query.value ?: Any())
                FirestoreQueryType.GREATER_THAN_EQUAL_TO -> ref.whereGreaterThanOrEqualTo(query.key, query.value ?: Any())
                FirestoreQueryType.LESS_THAN_EQUAL_TO -> ref.whereLessThanOrEqualTo(query.key, query.value ?: Any())
                FirestoreQueryType.ARRAY_CONTAINS -> ref.whereArrayContains(query.key, query.value ?: Any())
            }
        }

    }
}