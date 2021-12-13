package com.paypay.converter.workers


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paypay.converter.adapters.getCurrencyValueInLocale
import com.paypay.converter.database.coredb.AppDatabase
import com.paypay.converter.interfaces.ConverterInterface
import com.paypay.converter.interfaces.CurrencyInterface
import com.paypay.converter.models.ConversionRate
import com.paypay.converter.models.ConversionRateListResponse
import com.paypay.converter.models.Currency
import com.paypay.converter.models.CurrencyListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConverterWorker( context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

       val context = applicationContext

        try {

            //  Load the currency from the currencyLayer API
            Currency.loadCurrencyList( context = context , object : CurrencyInterface {

                    override suspend fun onCurrencyListLoaded( currencyListResponse: CurrencyListResponse ) {

                        //  Process the list for the db
                        if (!currencyListResponse.currencies.isEmpty()) {
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
                val currencyKeys    = currencies.map { it -> it.symbol }

                //  This map will hold all the conversionRates is a form that allows us to avoid duplicating exchange rates
                //  Thus USDJPY pair will also be the same as JPYUSD pair along with the rate
                //  No longer using this. All references are now disabled. I will come back to this later
                //val currencyRateMap = emptyMap<String,Float>().toMutableMap()

                //Now we will loop through each currency and get the conversion rates
                currencies.parallelStream().forEach { currency: Currency ->

                    ConversionRate.getConversionRatesForCurrency(
                        context,
                        currency.symbol,
                        object : ConverterInterface {

                            override suspend fun onConversionRatesLoaded(currencyListResponse: ConversionRateListResponse) {

                                if (currencyListResponse.success) {

                                    if (!currencyListResponse.quotes.isEmpty()) {

                                        //  Let's prepare the data for the db and eventually save it
                                        val list: List<ConversionRate> =
                                            currencyListResponse.quotes.entries
                                                //  No longer using this. All references are now disabled. I will come back to this later
                                                .map {
                                                    ConversionRate(
                                                        it.key,
                                                        "1 ${currency.symbol} : ${it.value.getCurrencyValueInLocale()} ${it.key.takeLast(3)}",
                                                        it.value
                                                    )
                                                }

                                        //  Save the conversion list to the DB
                                        viewModelScope.launch (Dispatchers.IO) { AppDatabase.getInstance(context).conversionRateDao().insertMultiple(list)}

                                        //  Update the @currencyRateMap variable
                                        //  No longer using this. All references are now disabled. I will come back to this later
                                        /*currencyRateMap += list.associateBy(
                                            { it.symbol },
                                            { it.rateValue })*/
                                    }

                                }
                            }

                            override fun onConverterFalied(message: String) {
                                Log.e("", message)
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