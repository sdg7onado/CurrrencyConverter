package com.paypay.converter.interfaces

import com.paypay.converter.models.ConversionRateListResponse
import com.paypay.converter.models.CurrencyListResponse
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


interface CurrencyInterface {

    suspend fun onCurrencyListLoaded(currencyListResponse: CurrencyListResponse)
    fun onConverterFalied(message: String)

    companion object {

    }

}

interface ConverterInterface {

    suspend fun onConversionRatesLoaded(currencyListResponse: ConversionRateListResponse)
    fun onGetConversionRate(currencyListResponse: ConversionRateListResponse)
    fun onConverterFalied(message: String)

}


