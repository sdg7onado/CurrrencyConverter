/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter.database.coredb

import androidx.annotation.WorkerThread
import com.paypay.converter.database.daos.ConversionRateDao
import com.paypay.converter.models.ConversionRate
import kotlinx.coroutines.flow.Flow

class ConversionRateRepository(private val conversionRateDao: ConversionRateDao) {

    val allConversionRates: Flow<List<ConversionRate>> = conversionRateDao.getAllConversionRates()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(conversionRate: ConversionRate) {
        conversionRateDao.insert(conversionRate)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertMultiple(conversionRates: List<ConversionRate>) {
        conversionRateDao.insertMultiple(conversionRates = conversionRates)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        conversionRateDao.deleteAll()
    }
}