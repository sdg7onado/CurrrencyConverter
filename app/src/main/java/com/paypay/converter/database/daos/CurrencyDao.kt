package com.paypay.converter.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paypay.converter.models.Currency
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM tb_currencies ORDER BY symbol")
    fun getAllCurrencies(): Flow<List<Currency>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(currency: Currency)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(currencies: List<Currency>)

    //@Query("DELETE FROM tb_conversionrates")
    //suspend fun deleteAll()
}
