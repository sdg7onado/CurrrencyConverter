/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 2:01 PM
 *
 */

package com.paypay.converter.models

import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paypay.converter.ApplicationExtender
import com.paypay.converter.ApplicationExtender.Companion.MY_SOCKET_TIMEOUT_MS
import com.paypay.converter.BuildConfig
import com.paypay.converter.interfaces.CurrencyInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import kotlin.collections.HashMap


@Entity(tableName = "tb_currencies")
class Currency : Serializable, Comparable<Currency?> {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "symbol")
    var symbol: String

    @NonNull
    @ColumnInfo(name = "name")
    var name: String

    @Ignore
    var rateValue: Double = 0.0

    @Ignore
    var rateConvertedValue: String = "0.00"

    //constructor() {}

    constructor( symbol: String, name: String ) {
        this.symbol = symbol
        this.name = name
    }

    constructor( symbol: String, name: String, rateValue: Double ) {
        this.symbol     = symbol
        this.name       = name
        this.rateValue  = rateValue
    }

    constructor( symbol: String, name: String, rateConvertedValue: String ) {
        this.symbol     = symbol
        this.name       = name
        this.rateConvertedValue  = rateConvertedValue
    }

    override fun compareTo(other: Currency?): Int {
        return symbol.compareTo(other!!.symbol, ignoreCase = true)
    }

    override fun toString(): String {
        return "Currency{" +
                "name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Currency

        if (symbol != other.symbol) return false
        if (name != other.name) return false
        if (rateValue != other.rateValue) return false
        if (rateConvertedValue != other.rateConvertedValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + rateValue.hashCode()
        result = 31 * result + rateConvertedValue.hashCode()
        return result
    }


    companion object : ViewModel() {

        suspend fun loadCurrencyList(
            context: Context?,
            currencyInterface: CurrencyInterface
        ) {

            //http://api.currencylayer.com/list?access_key=YOUR_ACCESS_KEY
            val url = String.format(Locale.getDefault(), BuildConfig.CURRENCYLAYER_LIST_ENDPOINT , BuildConfig.CURRENCYLAYER_BASE_URL, BuildConfig.CURRENCYLAYER_API_KEY)

            // Initialize a new JsonArrayRequest instance
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                url,
                null,
                Response.Listener { response: JSONObject? ->

                    try {

                        val gson        = Gson()
                        val listType    = object : TypeToken<CurrencyListResponse>(){}.type
                        val currencyListResponse: CurrencyListResponse  = gson.fromJson(response.toString(), listType)

                        if ( currencyListResponse.success ) {
                            viewModelScope.launch(Dispatchers.Main) {
                                currencyInterface.onCurrencyListLoaded(currencyListResponse)
                            }
                        } else {
                            currencyInterface.onConverterFalied(String.format(Locale.getDefault(), "No data pulled from the API / %s",currencyListResponse.error.info ) )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    error.message?.let { currencyInterface.onConverterFalied(message = it) }
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
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            ApplicationExtender.getInstance(context)?.addToRequestQueue(request)
        }

    }
}

class CurrencyListResponse (
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
    val currencies: Map<String, String> = emptyMap(),

    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val error: Error
) {

    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<CurrencyListResponse>(json)
    }
}

data class Error (
    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val code: Int,

    @get:JsonProperty(required=true)@field:JsonProperty(required=true)
    val info: String
)

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy =  PropertyNamingStrategies.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}