package com.paypay.converter.models

import androidx.lifecycle.*
import com.paypay.converter.database.coredb.ConversionRateRepository
import kotlinx.coroutines.launch


class ConversionRateViewModel(private val repository: ConversionRateRepository) : ViewModel() {

    // Using LiveData and caching what allCurrencies returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allConversionRates: LiveData<List<ConversionRate>> = repository.allConversionRates.asLiveData()

    fun insert(ConversionRate: ConversionRate) = viewModelScope.launch {
        repository.insert( ConversionRate )
    }

    fun insertMultiple(conversionRates: Map<String,Float>) = viewModelScope.launch {

        val list: List<ConversionRate> = conversionRates.entries.map { ConversionRate(it.key, it.value) }

        repository.insertMultiple( list )
    }

    fun insertMultiple(conversionRates: List<ConversionRate>) = viewModelScope.launch {
        repository.insertMultiple( conversionRates )
    }

    /*fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }*/

}

class ConversionRateViewModelFactory(private val repository: ConversionRateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversionRateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConversionRateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}