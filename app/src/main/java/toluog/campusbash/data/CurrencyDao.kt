package toluog.campusbash.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import toluog.campusbash.model.Currency
import toluog.campusbash.utils.AppContract

/**
 * Created by oguns on 3/2/2018.
 */
@Dao
public interface CurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCurrency(currency: Currency)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCurrencies(currencies: List<Currency>)

    @Update
    fun updateCurrencies(currencies: List<Currency>)

    @Update
    fun updateCurrency(currency: Currency)

    @Query("SELECT * FROM ${AppContract.CURRENCY_TABLE} WHERE id LIKE :id LIMIT 1")
    fun getCurrency(id: String): LiveData<Currency>

    @Query("SELECT * FROM ${AppContract.CURRENCY_TABLE}")
    fun getCurrencies(): LiveData<List<Currency>>

    @Delete()
    fun deleteCurrency(currency: Currency)

    @Query("DELETE FROM ${AppContract.CURRENCY_TABLE}")
    fun nukeTable()
}