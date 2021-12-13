/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 2:02 PM
 *
 */

package com.paypay.converter

import com.paypay.converter.models.ConversionRate
import com.paypay.converter.models.Currency
import junit.framework.Assert.assertEquals
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal

class ConversionUnitTest {

    private val conversionRate = ConversionRate()

    @Test
    fun `Assert Conversion Rates`() {

        //The soruce currencies with their exchange rates
        val conversionRates = listOf(
            ConversionRate( symbol = "USDAED" , rateDescription = "1 USD : 3.67296 AED"     , rateValue = 3.67296       ),
            ConversionRate( symbol = "USDAFN" , rateDescription = "1 USD : 103.255653 AFN"  , rateValue = 103.255653    ),
            ConversionRate( symbol = "USDALL" , rateDescription = "1 USD : 107.067669 ALL"  , rateValue = 107.067669    ),
            ConversionRate( symbol = "USDAMD" , rateDescription = "1 USD : 493.850311 AMD"  , rateValue = 493.850311    ),
            ConversionRate( symbol = "USDANG" , rateDescription = "1 USD : 1.795888 ANG"    , rateValue = 1.795888      )
        )

        //The selected currency
        var selectedCurrency = "USD"

        //The input amount
        val referenceAmount = BigDecimal("100")

        //The expected results
        val expectedConversionRates = arrayListOf(
            Currency( symbol = "AED" , name = "1 USD : 3.67296 AED"      , rateConvertedValue = "367.296"        ),
            Currency( symbol = "AFN" , name = "1 USD : 103.255653 AFN"   , rateConvertedValue = "10,325.5653"    ),
            Currency( symbol = "ALL" , name = "1 USD : 107.067669 ALL"   , rateConvertedValue = "10,706.7669"    ),
            Currency( symbol = "AMD" , name = "1 USD : 493.850311 AMD"   , rateConvertedValue = "49,385.0311"    ),
            Currency( symbol = "ANG" , name = "1 USD : 1.795888 ANG"     , rateConvertedValue = "179.5888"       )
        )

        val finalConversionRates = ConversionRate.computeConversionRates( conversionRates , selectedCurrency, referenceAmount )

        //  Lets test our conversion for the rates
        assertEquals( expectedConversionRates , finalConversionRates )
    }

}