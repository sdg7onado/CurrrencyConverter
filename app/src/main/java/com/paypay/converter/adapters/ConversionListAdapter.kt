/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.paypay.converter.R
import com.paypay.converter.models.Currency
import com.paypay.converter.models.toCurrencyValueInLocale
import java.util.*
import kotlin.collections.ArrayList


class ConversionListAdapter( private val context: Context, data: ArrayList<Currency>, listener: ConversionListAdapterClassAdapterListener) : RecyclerView.Adapter<RecyclerView.ViewHolder> (), Filterable{

    private val inflater        : LayoutInflater = LayoutInflater.from(context)

    var currencyList            : ArrayList<Currency> = ArrayList()
    var currencyListFiltered    : ArrayList<Currency> = ArrayList()
    var listener                : ConversionListAdapterClassAdapterListener
    private var lastPosition    = -1

    // Inflate the layout when viewholder created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = inflater.inflate(R.layout.cell_currency_list, parent, false)
        return CurrencyListHolder(view)
    }

    // Bind data
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        //onBindViewHolder(holder = holder, position = position, payloads = emptyList())

        ///*
        // Get current position of item in recyclerview to bind data and assign values from list
        val currencyListHolder                      = holder as CurrencyListHolder
        val currency                                = currencyListFiltered[position]

        val currencyName                            = currency.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        currencyListHolder.cellcurrencySymbol.text = currency.symbol
        currencyListHolder.cellcurrencyRateValue.text  = currency.rateConvertedValue
        currencyListHolder.cellcurrencyName.text   = currencyName

        setAnimation(currencyListHolder.itemView, position)
        //*/
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {

        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val o = payloads[0] as Bundle
            for (key in o.keySet()) {

                if (key == "rateConvertedValue") {

                    val currencyListHolder                      = holder as CurrencyListHolder
                    val currency                                = currencyListFiltered[position]

                    val currencyName                            = currency.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    currencyListHolder.cellcurrencySymbol.text = currency.symbol

                    //let's do some work on the rateValue
                    currencyListHolder.cellcurrencyRateValue.text  = currency.rateConvertedValue
                    currencyListHolder.cellcurrencyName.text       = currencyName
                }
            }
        }

        setAnimation(holder.itemView, position)
    }

    /**
     * Here is the key method to apply the animation
     */
    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    // return total item from List
    override fun getItemCount(): Int {
        return currencyListFiltered.size
    }

    fun notify(list: ArrayList<Currency>) {

        try {

            val diffResult = DiffUtil.calculateDiff(CurrencyComparator(list, currencyList))
            diffResult.dispatchUpdatesTo(this)
            currencyList.clear()
            currencyList.addAll(list)

        } catch ( e: Exception) {
            e.message?.let { Log.e("error", it) }
        }
    }

    //For the Filter
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                var charString = charSequence.toString()
                if (charString.isEmpty()) {
                    currencyListFiltered = currencyList
                } else {
                    charString = charString.lowercase()
                    val filteredList: ArrayList<Currency> = ArrayList()
                    for (row in currencyList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if ( row.name.contains(charString , ignoreCase = true)
                            || row.symbol.contains(charString , ignoreCase = true)
                        ) {
                            filteredList.add(row)
                        }
                    }
                    currencyListFiltered = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = currencyListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                results.values?.let {
                    notify(list = it as ArrayList<Currency>)
                }
            }
        }
    }

    //Listener
    interface ConversionListAdapterClassAdapterListener {
        fun onConversionRateSelected(currency: Currency?)
    }

    //For theViewHolder
    inner class CurrencyListHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var cellcurrencySymbol: TextView    = itemView.findViewById(R.id.cell_currencySymbol)
        var cellcurrencyName: TextView      = itemView.findViewById(R.id.cell_currencyName)
        var cellcurrencyRateValue: TextView = itemView.findViewById(R.id.cell_currencyRateValue)

        // create constructor to get widget reference
        init {

            itemView.setOnClickListener {
                // send selected contact in callback
                listener.onConversionRateSelected(currencyListFiltered[bindingAdapterPosition])
            }
        }
    }

    init {
        currencyList = data
        currencyListFiltered = data
        this.listener = listener
    }


    class CurrencyComparator(private var newList: ArrayList<Currency>, private var oldList: ArrayList<Currency>) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return newList[newItemPosition].symbol == oldList[oldItemPosition].symbol
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val result = newList[newItemPosition].rateValue.compareTo(oldList[oldItemPosition].rateValue) +
                            newList[newItemPosition].name.compareTo(oldList[oldItemPosition].name) +
                                newList[newItemPosition].rateConvertedValue.compareTo(oldList[oldItemPosition].rateConvertedValue)
            return result == 0
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {

            val newCurrency: Currency = newList[newItemPosition]
            val oldCurrency: Currency = oldList[oldItemPosition]

            if (oldCurrency.symbol == newCurrency.symbol) {
                return if ( oldCurrency.rateConvertedValue == newCurrency.rateConvertedValue ) {
                    super.getChangePayload(oldItemPosition, newItemPosition)
                } else {
                    val diff = Bundle()
                    diff.putString("rateConvertedValue", newCurrency.rateConvertedValue)
                    diff
                }
            }

            return super.getChangePayload(oldItemPosition, newItemPosition)

        }
    }
}
