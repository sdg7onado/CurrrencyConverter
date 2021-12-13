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
import java.util.*
import kotlin.collections.HashMap


@Entity(tableName = "tb_conversionrates")
class ConversionRate(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "symbol") var symbol: String,
    @NonNull
    @ColumnInfo(name = "rateDescription") var rateDescription: String,
    @NonNull
    @ColumnInfo(name = "rateValue") var rateValue: Float
) : Serializable, Comparable<ConversionRate?> {

    //constructor() {}

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
                            converterInterface.onConverterFalied(String.format(Locale.getDefault(), "No conversion data pulled from the API / %s",conversionRateListResponse.error.info ) )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    error.message?.let { converterInterface.onConverterFalied( message = it) }
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

        //Get the conversiob rate between two currencies
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
                            converterInterface.onConverterFalied(String.format(Locale.getDefault(), "No data pulled from the API / %s",conversionRateListResponse.error.info ) )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    error.message?.let { converterInterface.onConverterFalied( message = it) }
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

        val computeConversionRates = fun ( conversionRates : List<ConversionRate> , selectedCurrency : Currency , amount : Float ) : ArrayList<Currency> {

            return conversionRates
                .filter {
                    it -> it.symbol.take(3).equals(selectedCurrency.symbol)
                }
                .map {
                    it -> Currency ( symbol = it.symbol , name = it.rateDescription , rateValue = it.rateValue * amount )
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
    val quotes: Map<String, Float>,

    @get:JsonProperty(required=true)
    @field:JsonProperty(required=true)
    val error: Error
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<ConversionRateListResponse>(json)
    }
}
