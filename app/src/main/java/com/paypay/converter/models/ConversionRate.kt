/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 12:25 PM
 *
 */

package com.paypay.converter.models

import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paypay.converter.ApplicationExtender
import com.paypay.converter.BuildConfig
import com.paypay.converter.interfaces.ConverterInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.Serializable
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * @ConversionRate The conversion rate class
 *
 * @property symbol
 * @property rateDescription
 * @property rateValue
 */
@Entity(tableName = "tb_conversionrates")
class ConversionRate : Serializable, Comparable<ConversionRate?> {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "symbol")
    lateinit var symbol: String

    @NonNull
    @ColumnInfo(name = "rateDescription")
    lateinit var rateDescription: String

    @NonNull
    @ColumnInfo(name = "rateValue")
    var rateValue: Double = 0.0

    constructor() {}

    constructor(symbol: String, rateDescription: String, rateValue: Double) {
        this.symbol = symbol
        this.rateDescription = rateDescription
        this.rateValue = rateValue
    }


    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: ConversionRate?): Int {
        return symbol.compareTo(other!!.symbol, ignoreCase = true)
    }

    override fun toString(): String {
        return "ConversionRate{" +
                "symbol='" + symbol + '\'' +
                ", rateValue='" + rateValue + '\'' +
                '}'
    }

    companion object : ViewModel() {

        //Get the conversion rates for a selected currency
        fun getConversionRatesForCurrency(
            context             : Context?,
            sourceCurrency      : String,
            converterInterface  : ConverterInterface
        ) {

            //E.g: http://api.currencylayer.com/live?access_key=YOUR_ACCESS_KEY&source=SOURCE_CURRENCY
            val url = java.lang.String.format(
                Locale.UK,
                BuildConfig.CURRENCYLAYER_RATES_ENDPOINT,
                BuildConfig.CURRENCYLAYER_BASE_URL,
                BuildConfig.CURRENCYLAYER_API_KEY,
                sourceCurrency
            )

            // Initialize a new JsonArrayRequest instance
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                url,
                null,
                Response.Listener { response: JSONObject? ->

                    try {

                        val gson        = Gson()
                        val listType    = object : TypeToken<ConversionRateListResponse>(){}.type
                        val conversionRateListResponse: ConversionRateListResponse  = gson.fromJson(response.toString(), listType)

                        if ( conversionRateListResponse.success ) {
                            viewModelScope.launch(Dispatchers.Main) {
                                converterInterface.onConversionRatesLoaded( conversionRateListResponse )
                            }
                        } else {
                            converterInterface.onConverterFalied(conversionRateListResponse,String.format(Locale.getDefault(), "No conversion data pulled from the API / %s",conversionRateListResponse.error.info ) )
                            //converterInterface.onConverterFalied(String.format(Locale.getDefault(), "No conversion data pulled from the API / %s",conversionRateListResponse.error.info ) )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    error.message?.let { converterInterface.onConverterFalied( null , message = it) }
                }) {

                //This is for Headers If You Needed
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    //params["Content-Type"] = "application/json; charset=UTF-8"
                    return params
                }
            }
            request.retryPolicy = DefaultRetryPolicy(
                ApplicationExtender.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            ApplicationExtender.getInstance(context)?.addToRequestQueue(request)
        }

        //Get the conversion rate between two currencies
        fun getConversionRate(
            context: Context?,
            fromCurrency:String,
            toCurrency:String,
            amount:Float,
            converterInterface: ConverterInterface
        ) {

            //E.g: http://api.currencylayer.com/convert?access_key=YOUR_ACCESS_KEY&from=FROMCURRENCY&to=TOCURRENCY&amount=AMOUNT
            val url = java.lang.String.format(
                Locale.UK,
                BuildConfig.CURRENCYLAYER_CONVERT_ENDPOINT,
                BuildConfig.CURRENCYLAYER_BASE_URL,
                BuildConfig.CURRENCYLAYER_API_KEY,
                fromCurrency,
                toCurrency,
                amount,
            )

            // Initialize a new JsonArrayRequest instance
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                url,
                null,
                Response.Listener { response: JSONObject? ->

                    try {

                        val gson        = Gson()
                        val listType    = object : TypeToken<ConversionRateListResponse>(){}.type
                        val conversionRateListResponse: ConversionRateListResponse  = gson.fromJson(response.toString(), listType)

                        if ( conversionRateListResponse.success ) {
                            viewModelScope.launch(Dispatchers.Main) {
                                converterInterface.onGetConversionRate( conversionRateListResponse )
                            }
                        } else {
                            converterInterface.onConverterFalied(conversionRateListResponse,String.format(Locale.getDefault(), "No data pulled from the API / %s",conversionRateListResponse.error.info ) )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    error.message?.let { converterInterface.onConverterFalied( null,message = it) }
                }) {

                //This is for Headers If You Needed
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    //params["Content-Type"] = "application/json; charset=UTF-8"
                    return params
                }
            }
            request.retryPolicy = DefaultRetryPolicy(
                ApplicationExtender.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            ApplicationExtender.getInstance(context)?.addToRequestQueue(request)
        }

        fun computeConversionRates( conversionRates : List<ConversionRate> , selectedCurrencySymbol : String , amount : BigDecimal = BigDecimal.ZERO ) : ArrayList<Currency> {

            //  In the filter, we select all matches where the first 3 characters == the selectedCurrency
            //  In the map, we create a new list comprising of all the matches from the filter along with their @symbol, @rateDescription
            //  and calculated exchange rate for the provided amount

            return conversionRates
                .filter {
                    //
                    it.symbol.take(3) == selectedCurrencySymbol
                }
                .map {
                    Currency ( symbol = it.symbol.takeLast(3) , name = it.rateDescription , rateConvertedValue = BigDecimal(it.rateValue).multiply(amount).toCurrencyValueInLocale() )
                } as ArrayList<Currency>

        }

    }

}

open class ConversionRateListResponse (
    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val success: Boolean,

    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val terms: String,

    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val privacy: String,

    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val timestamp: Long,

    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val source: String,

    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val quotes: Map<String, Double>,

    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val error: Error
) {

    companion object {}
}

fun Double.toCurrencyValueInLocale(): String {
    val format = DecimalFormat("#,###,###.#######")
    return format.format(this)
}

fun BigDecimal.toCurrencyValueInLocale(): String {
    val format = DecimalFormat("#,###,###.#######")
    return format.format(this)
}
