/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter.database.coredb

import androidx.annotation.WorkerThread
import com.paypay.converter.database.daos.CurrencyDao
import com.paypay.converter.models.Currency
import kotlinx.coroutines.flow.Flow

class CurrencyRepository(private val currencyDao: CurrencyDao) {

    val allCurrencies: Flow<List<Currency>?> = currencyDao.getAllCurrencies()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(currency: Currency) {
        currencyDao.insert(currency)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertMultiple(currencies: List<Currency>) {
        currencyDao.insertMultiple(currencies = currencies)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        currencyDao.deleteAll()
    }
}