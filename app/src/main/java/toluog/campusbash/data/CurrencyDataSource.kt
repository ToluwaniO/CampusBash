package toluog.campusbash.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.experimental.launch
import toluog.campusbash.model.Currency
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract


/**
 * Created by oguns on 3/2/2018.
 */
class CurrencyDataSource {
    companion object {
        val mFireStore = FirebaseFirestore.getInstance()
        val query = mFireStore.collection(AppContract.FIREBASE_CURRENCIES)
        var db: AppDatabase? = null
        val TAG = CurrencyDataSource::class.java.simpleName

        fun initListener(context: Context){
            Log.d(TAG, "initListener")
            db = AppDatabase.getDbInstance(context)
            val currDao = db?.currencyDao()
            query.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.d(TAG, "onEvent:error", e)
                    return@EventListener
                }

                // Dispatch the event
                for (change in value.getDocumentChanges()) {
                    // Snapshot of the changed document
                    Log.d(TAG, change.document.toString())
                    val snapshot = change.document.toObject(Currency::class.java)

                    when (change.getType()) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(TAG, "ChildAdded")
                            launch { currDao?.insertCurrency(snapshot) }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d(TAG, "ChildModified")
                            launch { currDao?.updateCurrency(snapshot) }
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(TAG, "ChildRemoved")
                            launch { currDao?.deleteCurrency(snapshot) }
                        }
                    }
                }
            })
        }

        fun downloadCurrencies(context: Context) {
            db = AppDatabase.getDbInstance(context)
            val currDao = db?.currencyDao()
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val snapshot = document.toObject(Currency::class.java)
                        launch { currDao?.insertCurrency(snapshot) }
                        Log.d(TAG, "$snapshot")
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            }
        }
    }
}