/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter.interfaces

import com.paypay.converter.models.ConversionRateListResponse
import com.paypay.converter.models.CurrencyListResponse


interface CurrencyInterface {

    suspend fun onCurrencyListLoaded(currencyListResponse: CurrencyListResponse)
    fun onConverterFalied(message: String)

    companion object {

    }

}

interface ConverterInterface {

    suspend fun onConversionRatesLoaded(currencyListResponse: ConversionRateListResponse)
    fun onGetConversionRate(currencyListResponse: ConversionRateListResponse)
    fun onConverterFalied(currencyListResponse: ConversionRateListResponse?,message: String)

}


