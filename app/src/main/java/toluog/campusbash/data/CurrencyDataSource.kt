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

        var db: AppDatabase? = null
        val TAG = CurrencyDataSource::class.java.simpleName

        fun initListener(mFirestore: FirebaseFirestore, context: Context){
            Log.d(TAG, "initListener")
            val query = mFirestore.collection(AppContract.FIREBASE_CURRENCIES)
            db = AppDatabase.getDbInstance(context)
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

        fun downloadCurrencies(mFirestore: FirebaseFirestore, context: Context) {
            Log.d(TAG, "Attempting to download currencies")
            db = AppDatabase.getDbInstance(context)
            val currDao = db?.currencyDao()
            val query = mFirestore.collection(AppContract.FIREBASE_CURRENCIES)
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    launch {
                        for (document in task.result) {
                            if(document.exists()) {
                                val snapshot = document.toObject(Currency::class.java)
                                currDao?.insertCurrency(snapshot)
                                Log.d(TAG, "$snapshot")
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            }
        }
    }
}