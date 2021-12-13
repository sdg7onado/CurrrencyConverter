package com.paypay.converter.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paypay.converter.models.ConversionRate
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversionRateDao {

    @Query("SELECT * FROM tb_conversionrates ORDER BY symbol")
    fun getAllConversionRates(): Flow<List<ConversionRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversionRate: ConversionRate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(conversionRates: List<ConversionRate>)

    //@Query("DELETE FROM tb_conversionrates")
    //suspend fun deleteAll()

}