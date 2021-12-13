/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter.models

import androidx.lifecycle.*
import com.paypay.converter.database.coredb.CurrencyRepository
import kotlinx.coroutines.launch


class CurrencyViewModel(private val repository: CurrencyRepository) : ViewModel() {

    // Using LiveData and caching what allCurrencies returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allCurrencies: LiveData<List<Currency>?> = repository.allCurrencies.asLiveData()

    fun insert(currency: Currency) = viewModelScope.launch {
        repository.insert( currency )
    }

    fun insertMultiple(currencies: Map<String,String>) = viewModelScope.launch {

        val list: List<Currency> = currencies.entries.map { Currency(it.key, it.value) }

        repository.insertMultiple( list )
    }

    fun insertMultiple(currencies: ArrayList<Currency>) = viewModelScope.launch {
        repository.insertMultiple( currencies )
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

}

class CurrencyViewModelFactory(private val repository: CurrencyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrencyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CurrencyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}