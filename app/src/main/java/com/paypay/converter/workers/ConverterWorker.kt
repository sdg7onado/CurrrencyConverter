/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter.workers


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paypay.converter.database.coredb.AppDatabase
import com.paypay.converter.interfaces.ConverterInterface
import com.paypay.converter.interfaces.CurrencyInterface
import com.paypay.converter.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ConverterWorker( context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

       val context = applicationContext

        try {

            //  Load the currency from the currencyLayer API
            Currency.loadCurrencyList( context = context , object : CurrencyInterface {

                    override suspend fun onCurrencyListLoaded( currencyListResponse: CurrencyListResponse ) {

                        //  Process the list for the db
                        if (currencyListResponse.currencies.isNotEmpty()) {
                            val list: List<Currency> =
                                currencyListResponse.currencies.entries.map {
                                    Currency( it.key, it.value )
                                }
                            AppDatabase.getInstance(context).currencyDao().insertMultiple(list)

                            //  load the conversion list
                            loadConversionList( currencies = list , context = context )
                        } else {
                            Result.retry()
                        }

                    }

                    override fun onConverterFalied(message: String) {}

                }

            )

        } catch (ex: Exception) {
            Log.e(TAG, "Error loading data", ex)
            Result.failure()
        }

        return Result.success()
    }

    companion object : ViewModel() {

        private const val TAG = "ConverterWorker"

        suspend fun loadConversionList( currencies : List<Currency> , context : Context ) {

            try {

                //  Lets hold a list pf all our currencies here for the iteration.
                //  We will avoid duplicating the list by extracting the symbols only and save on space
                //val currencyKeys    = currencies.map { it -> it.symbol }

                //Now we will loop through each currency and get the conversion rates
                currencies.parallelStream().forEach { currency: Currency ->

                    ConversionRate.getConversionRatesForCurrency(
                        context,
                        currency.symbol,
                        object : ConverterInterface {

                            override suspend fun onConversionRatesLoaded(currencyListResponse: ConversionRateListResponse) {

                                if (currencyListResponse.success) {

                                    if (currencyListResponse.quotes.isNotEmpty()) {

                                        //  Let's prepare the data for the db and eventually save it
                                        val list: List<ConversionRate> =
                                            currencyListResponse.quotes.entries
                                                .map {
                                                    ConversionRate(
                                                        it.key,
                                                        "1 ${currency.symbol} : ${it.value.toCurrencyValueInLocale()} ${it.key.takeLast(3)}",
                                                        it.value
                                                    )
                                                }

                                        //  Save the conversion list to the DB
                                        viewModelScope.launch (Dispatchers.IO) { AppDatabase.getInstance(context).conversionRateDao().insertMultiple(list)}
                                    }

                                }
                            }

                            override fun onConverterFalied(currencyListResponse: ConversionRateListResponse?, message: String) {
                                Log.e("", message)
                                currencyListResponse?.let {
                                    if (it.error.code == 106) {
                                        //  Handles the error:
                                        //  Error(code=106, info=You have exceeded the maximum rate limitation allowed on your subscription plan.
                                        //  Please refer to the "Rate Limits" section of the API Documentation for details. )
                                        //  TODO: This approach will override the previous values. FInd a way around that
                                        val list: List<ConversionRate> =
                                            currencies
                                                .map {
                                                    ConversionRate(
                                                        currency.symbol+it.symbol,
                                                        "1 ${currency.symbol} : ${1.0.toCurrencyValueInLocale()} ${it.name}",
                                                        1.0
                                                    )
                                                }

                                        //  Save the conversion list to the DB
                                        viewModelScope.launch (Dispatchers.IO) { AppDatabase.getInstance(context).conversionRateDao().insertMultiple(list)}
                                    }
                                }
                            }

                            override fun onGetConversionRate(currencyListResponse: ConversionRateListResponse) {}
                        })
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Error loading data", ex)
            }
        }
    }

}