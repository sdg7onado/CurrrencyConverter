package com.paypay.converter.models

import java.util.*


interface ConverterInterface {

    fun onCurrenciesLoaded(message: String?, currencies: ArrayList<Currency>)

    fun onCurrenciesLoadFalied(message: String)

}