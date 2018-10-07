package toluog.campusbash.data.datasource

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.experimental.launch
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.model.Currency
import toluog.campusbash.utils.AppContract

class CurrencyDataSource(context: Context): DataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val db = AppDatabase.getDbInstance(context)

    companion object {
        private val TAG = CurrencyDataSource::class.java.simpleName

        fun downloadCurrencies(context: Context) {
            Log.d(TAG, "Attempting to download currencies")
            val db = AppDatabase.getDbInstance(context)
            val query = FirebaseFirestore.getInstance().collection(AppContract.FIREBASE_CURRENCIES)
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    launch {
                        for (document in task.result) {
                            if(document.exists() && validate(document)) {
                                val snapshot = document.toObject(Currency::class.java)
                                db?.currencyDao()?.insertCurrency(snapshot)
                                Log.d(TAG, "$snapshot")
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            }
        }

        private fun validate(doc: QueryDocumentSnapshot): Boolean {
            if(doc["id"] == null) return false
            if(doc["name"] == null) return false
            if(doc["namePlural"] == null) return false
            if(doc["symbol"] == null) return false
            if(doc["symbolNative"] == null) return false
            if(doc["code"] == null) return false
            if(doc["rounding"] == null) return false
            if(doc["decimalDigits"] == null) return false
            return true
        }
    }

    fun listenForCurrencies(){
        Log.d(TAG, "initListener")
        val query = firestore.collection(AppContract.FIREBASE_CURRENCIES)
        val currDao = db?.currencyDao()
        query.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.d(TAG, "onEvent:error", e)
                return@EventListener
            }

            // Dispatch the event
            launch {
                value?.documentChanges?.forEach {
                    // Snapshot of the changed document
                    if(it.document.exists()) {
                        Log.d(TAG, it.document.toString())
                        val snapshot = it.document.toObject(Currency::class.java)

                        when (it.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.d(TAG, "ChildAdded")
                                currDao?.insertCurrency(snapshot)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Log.d(TAG, "ChildModified")
                                currDao?.updateCurrency(snapshot)
                            }
                            DocumentChange.Type.REMOVED -> {
                                Log.d(TAG, "ChildRemoved")
                                currDao?.deleteCurrency(snapshot)
                            }
                        }
                    }
                }
            }
        })
    }

    override fun clear() {

    }

}