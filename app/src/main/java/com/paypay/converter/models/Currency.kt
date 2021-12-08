package com.paypay.converter.models

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.*
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.paypay.converter.ApplicationExtender
import com.paypay.converter.ApplicationExtender.Companion.MY_SOCKET_TIMEOUT_MS
import com.paypay.converter.BuildConfig
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

@Entity(tableName = "tb_currencies", indices = [Index(value = ["symbol"], unique = true)])
class Currency : Serializable, Comparable<Currency?> {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "symbol")
    @SerializedName("symbol")
    @Expose
    lateinit var symbol: String

    @NonNull
    @ColumnInfo(name = "name")
    @SerializedName("name")
    @Expose
    lateinit var name: String

    @SerializedName("rateValue")
    var rateValue: Float? = null

    @Ignore
    constructor() {}

    constructor(
        symbol: String,
        name: String
    ) {
        this.symbol = symbol
        this.name = name
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

    companion object {

        fun loadCurrencyList(
            context: Context?,
            converterInterface: ConverterInterface
        ) {

            //https://api.currencylayer.com/list? access_key = YOUR_ACCESS_KEY

            /*val url = java.lang.String.format(
                Locale.UK,
                "%slist?access_key=%s",
                BuildConfig.CURRENCYLAYER_URL,
                BuildConfig.CURRENCYLAYER_API_KEY
            )*/

            val url = "${BuildConfig.CURRENCYLAYER_URL}list?access_key=${BuildConfig.CURRENCYLAYER_API_KEY}."

            // Initialize a new JsonArrayRequest instance
            val request: StringRequest = object : StringRequest(
                Method.GET,
                url,
                Response.Listener { response: String? ->
                    // Process the RESPONSE
                    try {
                        //getFilePhotoCallbackInterface.onPhotoLoaded("", response)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    //getFilePhotoCallbackInterface.onPhotoLoaded(error.message, null)
                }) {
                //This is for Headers If You Needed
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    //params["Content-Type"] = "application/json; charset=UTF-8"
                    //params["Authorization"] = String.format(Locale.UK, "Bearer %s", jwt)
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

        /*
        fun loadAllBeneficiaries(
            context: Context?,
            jwt: String?,
            ptid: Long,
            converterInterface: ConverterInterface
        ) {
            val parameters = HashMap<String, Any>()
            parameters["lastPtid"] = ptid
            parameters["rowCount"] = BuildConfig.APPLICATION_ID
            val query = parameters.entries.stream()
                .map { p: Map.Entry<String, Any> -> p.key + "=" + p.value }
                .reduce { p1: String, p2: String -> "$p1&$p2" }
                .orElse("")

            //String url = String.format(Locale.UK, "%s/Currency/%d/%d", BuildConfig.ROOTURL, ptid, BuildConfig.ROWCOUNT);
            val url =
                java.lang.String.format(Locale.UK, "%sCurrency?%s", BuildConfig.ROOTURL, query)

            // Initialize a new JsonArrayRequest instance
            val request: JsonArrayRequest = object : JsonArrayRequest(
                Method.GET,
                url,
                null,
                Response.Listener { response: JSONArray ->
                    // Process the JSON
                    try {

                        // Loop through the array elements
                        val gson = Gson()
                        val listType =
                            object : TypeToken<ArrayList<Currency?>?>() {}.type
                        val currencies: ArrayList<Currency> =
                            gson.fromJson(response.toString(), listType)
                        converterInterface.onCurrenciesLoaded("", currencies)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    converterInterface.onCurrenciesLoaded(error.message, null)
                }) {
                //This is for Headers If You Needed
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json; charset=UTF-8"
                    params["Authorization"] = String.format(Locale.UK, "Bearer %s", jwt)
                    return params
                }
            }
            request.retryPolicy = DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            ApplicationExtender.getInstance(context).addToRequestQueue(request)
        }*/


        /*
        fun makePayment(
            context: Context?,
            jwt: String?,
            makePaymentCallbackInterface: MakePaymentActivity.MakePaymentCallbackInterface
        ) {
            try {
                val url: String =  BuildConfig.ROOTURL.toString() + "Transaction/Submit"
                Log.e("URL", "" + url)
                val params = HashMap<String?, String?>()
                params["paymentId"] = paymentID
                params["paymentTimestamp"] = Utilities.getZuluTime(Date(System.currentTimeMillis()))
                params["CurrencyBase64"] = CurrencyBase64
                params["geolocation"] = location

                val parameters = JSONObject(params)
                val request: JsonObjectRequest = object : JsonObjectRequest(
                    Method.POST, url, parameters,
                    Response.Listener { response: JSONObject ->
                        Log.e("onResponse", "" + response)
                        try {
                            val gson = Gson()
                            val listType =
                                object : TypeToken<MakePaymentResponse?>() {}.type
                            val paymentResponse: MakePaymentResponse =
                                gson.fromJson(response.toString(), listType)

                            //callback to the source
                            makePaymentCallbackInterface.onPaymentComplete("", paymentResponse)
                        } catch (e: Exception) {
                            Log.e("Exception", "" + e)
                            val paymentResponse = MakePaymentResponse()
                            paymentResponse.setResponseDescription(e.message)
                            paymentResponse.setResponseCode("-1")
                            makePaymentCallbackInterface.onPaymentFailed(e.message, paymentResponse)
                        }
                    },
                    Response.ErrorListener { error: VolleyError ->
                        try {
                            var makePaymentErrorResponse =
                                MakePaymentErrorResponse()
                            val responseBody =
                                String(error.networkResponse.data, StandardCharsets.UTF_8)
                            makePaymentErrorResponse.setTitle(responseBody)

                            //check if the response is empty
                            if (!TextUtils.isEmpty(responseBody)) {
                                val gson = Gson()
                                makePaymentErrorResponse =
                                    gson.fromJson(responseBody, MakePaymentErrorResponse::class.java)
                                Log.e("MPER", makePaymentErrorResponse.toString())
                            }
                            makePaymentCallbackInterface.onPaymentFailed(
                                makePaymentErrorResponse.getTitle(),
                                null
                            )
                        } catch (e: Exception) {
                            val paymentResponse = MakePaymentResponse()
                            paymentResponse.setResponseDescription(e.message)
                            paymentResponse.setResponseCode("-1")
                            makePaymentCallbackInterface.onPaymentFailed(e.message, paymentResponse)
                        }
                    }) {
                    //This is for Headers If You Needed
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["Content-Type"] = "application/json; charset=UTF-8"
                        params["Authorization"] = String.format(Locale.UK, "Bearer %s", jwt)
                        return params
                    }
                }
                request.retryPolicy = DefaultRetryPolicy(
                    MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                AppAnalyticsExtender.getInstance(context).addToRequestQueue(request)
            } catch (e: Exception) {
                val paymentResponse = MakePaymentResponse()
                paymentResponse.setResponseDescription(e.message)
                paymentResponse.setResponseCode("-1")
                makePaymentCallbackInterface.onPaymentFailed(e.message, paymentResponse)
                e.printStackTrace()
            }
        }
        */

    }
}