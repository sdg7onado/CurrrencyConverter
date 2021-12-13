/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 2:41 PM
 *
 */

package com.paypay.converter

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ListenableWorker
import com.google.firebase.analytics.FirebaseAnalytics
import com.paypay.converter.adapters.ConversionListAdapter
import com.paypay.converter.adapters.CurrencySpinnerAdapter
import com.paypay.converter.database.coredb.AppDatabase
import com.paypay.converter.interfaces.CurrencyInterface
import com.paypay.converter.models.*
import com.paypay.converter.models.Currency
import com.paypay.converter.workers.ConverterWorker.Companion.loadConversionList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Double.parseDouble
import java.lang.Float.parseFloat
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.Locale
import kotlin.collections.ArrayList


class ConverterActivity : ControlActivity(),
    ConversionListAdapter.ConversionListAdapterClassAdapterListener
    {

    private val TAGNAME: String = ControlActivity::class.java.simpleName

    //  Controls
    private lateinit var spinnerReferenceCurrency: Spinner
    private lateinit var editTextReferenceAmount: EditText

    //  Adapters
    private lateinit var mCurrencySpinnerAdapter: CurrencySpinnerAdapter
    private lateinit var mConversionListAdapter: ConversionListAdapter

    //  Analytics
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    //  Objects
    private var mCurrencies     : List<Currency>        = emptyList()
    private var mConversionRates: List<ConversionRate> = emptyList()

    // selectedCurrency
    private var mSelectedCurrency : Currency? = null

        private val currencyViewModel: CurrencyViewModel by viewModels {
        CurrencyViewModelFactory((application as ApplicationExtender).currencyRepository)
    }

    private val conversionRateViewModel: ConversionRateViewModel by viewModels {
        ConversionRateViewModelFactory((application as ApplicationExtender).conversionRateRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Handle the splash screen transition.
        installSplashScreen()

        setContentView(R.layout.activity_converter)

        try {

            //for logging on firebase
            val bundle = Bundle()
            bundle.putString( FirebaseAnalytics.Param.SCREEN_NAME, ConverterActivity::class.java.simpleName )
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, TAGNAME)

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)

            //set the controls
            editTextReferenceAmount = findViewById(R.id.editTextReferenceAmount)
            spinnerReferenceCurrency = findViewById(R.id.spinnerReferenceCurrency)

            //Add the change listener for the @editTextReferenceAmount
            editTextReferenceAmount.addTextChangedListener(
                onEditTextChangedListener(
                    editTextReferenceAmount
                )
            )

            editTextReferenceAmount.doAfterTextChanged {
                computeConversionRates()
            }

            //  Display the Spinner and Recycler view (empty)
            setupCurrencySpinnerAdapter()
            setupConversionRateRecyclerView()

            //  this will hardly change so we can load it once during the app start-up process
            getCurrencyConversionListings()

            //setup the swipe refresh layout
            val converterSwipeRefresh: SwipeRefreshLayout = findViewById(R.id.converterSwipeRefresh)
            converterSwipeRefresh.setOnRefreshListener {
                computeConversionRates()
                converterSwipeRefresh.isRefreshing = false
            }

        } catch (e: Exception) {
            Toast.makeText(this@ConverterActivity, e.message, Toast.LENGTH_LONG).show()
            logExceptionEvent( exception = e , method = "onCreate" , analytics = mFirebaseAnalytics )
        }
    }

    private fun getCurrencyConversionListings() {

        try {

            //  We will be loading the currency using an observer but ( we should do this once since it will be relatively stable)
            //  Where it is empty we will load from the API
            //  It is expected that workmanager would have loaded this in the background
            currencyViewModel.allCurrencies.observe(this) { currencies ->

                val progressbar = findViewById<ProgressBar>(R.id.converterProgressBar)
                progressbar.visibility = View.GONE

                if (currencies == null || currencies.isEmpty()) {
                    //  Call the currency layer API through our Currency class to get the API list
                    viewModelScope.launch(Dispatchers.Default) {
                        loadCurrenciesFromAPI( applicationContext )
                    }
                } else {

                    //  Hide the empty state view
                    var emptyStateView = findViewById<RelativeLayout>(R.id.emptyStateView)
                    emptyStateView.visibility = View.GONE

                    //  Update the cached copy of the currencies in the adapter.
                    //  call to loadCurrencySpinnerAdapter
                    mCurrencies             = currencies
                    mCurrencySpinnerAdapter.notify(mCurrencies)

                    //  Get the conversions from the db/api
                    getConversionRateListings()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@ConverterActivity, e.message, Toast.LENGTH_LONG).show()
            logExceptionEvent( exception = e , method = "getCurrencyConversionListings" , analytics = mFirebaseAnalytics )
        }
    }

    /**
     * TODO
     *
     */
    private fun getConversionRateListings() {

        try {

            //
            computeConversionRates()

            //load the default list if any
            //mConversionListAdapter.notify( list = ArrayList(mCurrencies) )

            //  We will be loading the currency using an observer but ( we should do this once since it will be relatively stable)
            //  Where it is empty we will load from the API
            //  It is expected that workmanager would have loaded this in the background
            conversionRateViewModel.allConversionRates.observe(this) { conversionRates ->
                // Update the cached copy of the conversionRates in the adapter.
                if (conversionRates == null || conversionRates.isEmpty()) {
                    //call the convert currencyLayer API through our ConversionRate class
                    viewModelScope.launch(Dispatchers.Default) {
                        loadConversionList(
                            currencies = mCurrencies,
                            context = applicationContext
                        )
                    }
                } else {
                    //call to loadConversionRateRecyclerView
                    mConversionRates = conversionRates
                    computeConversionRates()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this@ConverterActivity, e.message, Toast.LENGTH_LONG).show()
            logExceptionEvent( exception = e , method = "getConversionListings" , analytics = mFirebaseAnalytics )
        }

    }

    private fun computeConversionRates() {

        try {

            //  Computes the exchange rate for each currency in @mCurrencies
            //  Get the value entered by the user If none return 0 to the variable
            val referenceAmount = if ( editTextReferenceAmount.text.isNotEmpty() ) { BigDecimal(editTextReferenceAmount.text.toString().replace(",","")) } else { BigDecimal("0") }

            //  Get the selectedCurrency or use the default currency (first on the list)
            mSelectedCurrency = mSelectedCurrency ?:run {mCurrencies.first()}

            //  THIS IS WHERE THE ACTUAL CONVERSION TAKES PLACE FOR THE ACTIVITY
            //  Force recheck of the rates for the selected currency
            //  The computation will be done when the observe method is called from this activity due to any new changes
            //  We call @ConversionRate.computeConversionRates() which return the list with the rates updated
            val updatedConversion = ConversionRate.computeConversionRates( mConversionRates , mSelectedCurrency!!.symbol, referenceAmount )
            mConversionListAdapter.notify( updatedConversion )


        } catch (e: Exception) {
            Toast.makeText(this@ConverterActivity, e.message, Toast.LENGTH_LONG).show()
            logExceptionEvent( exception = e , method = "computeConversionRates" , analytics = mFirebaseAnalytics )
        }

    }

    private fun setupCurrencySpinnerAdapter() {

        try {

            // specify an adapter (see also next example)
            mCurrencySpinnerAdapter = CurrencySpinnerAdapter(applicationContext, mCurrencies)
            spinnerReferenceCurrency.adapter = mCurrencySpinnerAdapter

            //
            spinnerReferenceCurrency.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    arg0: AdapterView<*>?, arg1: View,
                    arg2: Int, arg3: Long
                ) {
                    mSelectedCurrency = mCurrencies[arg2]
                    computeConversionRates()

                    //  Reload the exchange list for the selected currency
                    viewModelScope.launch(Dispatchers.Default) {
                        loadConversionList(
                            currencies = arrayListOf(mSelectedCurrency!!),
                            context = applicationContext
                        )
                    }
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            })

        } catch (e: Exception) {
            Toast.makeText(this@ConverterActivity, e.message, Toast.LENGTH_LONG).show()
            logExceptionEvent( exception = e , method = "setupCurrencySpinnerAdapter" , analytics = mFirebaseAnalytics )
        }

    }

    private fun setupConversionRateRecyclerView() {

        try {

            //  Set the recycler view from the activity
            val conversionsRecyclerView: RecyclerView = findViewById(R.id.conversionsRecyclerView)

            //  use a linear layout manager
            val mLayoutManager = LinearLayoutManager(this)
            conversionsRecyclerView.layoutManager = mLayoutManager

            //  Set a divider for the list
            val dividerItemDecoration = DividerItemDecoration(
                conversionsRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
            ContextCompat.getDrawable(
                conversionsRecyclerView.context,
                R.drawable.list_divider
            )?.let {
                dividerItemDecoration.setDrawable(
                    it
                )
            }
            conversionsRecyclerView.addItemDecoration(dividerItemDecoration)

            //  Specify an adapter (see also next example)
            mConversionListAdapter = ConversionListAdapter(applicationContext, ArrayList(mCurrencies), this)
            conversionsRecyclerView.adapter = mConversionListAdapter


        } catch (e: Exception) {
            Toast.makeText(this@ConverterActivity, e.message, Toast.LENGTH_LONG).show()
            logExceptionEvent( exception = e , method = "setupConversionRateRecyclerView" , analytics = mFirebaseAnalytics )
        }
    }

    override fun onConversionRateSelected(currency: Currency?) {}

    companion object : ViewModel() {

        private fun onEditTextChangedListener(editText: EditText): TextWatcher {

            return object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    editText.removeTextChangedListener(this)
                    try {
                        var originalString = s.toString()
                        if (originalString.contains(",")) {
                            originalString = originalString.replace(",".toRegex(), "")
                        }
                        val longval: Long = originalString.toLong()
                        val formatter: DecimalFormat =
                            NumberFormat.getInstance(Locale.getDefault()) as DecimalFormat
                        formatter.applyPattern("#,###,###,###")
                        val formattedString: String = formatter.format(longval)

                        //setting text after format to EditText
                        editText.setText(formattedString)
                        editText.setSelection(editText.text.length)
                    } catch (nfe: NumberFormatException) {
                        nfe.printStackTrace()
                    }
                    editText.addTextChangedListener(this)
                }
            }
        }

        private suspend fun loadCurrenciesFromAPI( context : Context ) {

            //  Load the currency from the currencyLayer API
            Currency.loadCurrencyList(context = context, object : CurrencyInterface {

                override suspend fun onCurrencyListLoaded(currencyListResponse: CurrencyListResponse) {

                    //  Process the list for the db
                    if (currencyListResponse.currencies.isNotEmpty()) {
                        val list: List<Currency> =
                            currencyListResponse.currencies.entries.map {
                                Currency(it.key, it.value)
                            }
                        AppDatabase.getInstance(context).currencyDao().insertMultiple(list)

                        //  load the conversion list
                        loadConversionList(currencies = list, context = context)
                    } else {
                        ListenableWorker.Result.retry()
                    }

                }

                override fun onConverterFalied(message: String) {}

            })
        }

        private fun logExceptionEvent( exception : Exception , method : String , analytics : FirebaseAnalytics ){
            val bundle = Bundle()
            bundle.putString( "controller", ConverterActivity::class.java.simpleName )
            bundle.putString( "method", method )
            bundle.putString( "exception", exception.message )

            analytics.logEvent("app_exception", bundle)
        }
    }
}
