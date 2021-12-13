package com.paypay.converter.adapters

import android.R.attr
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
import java.util.*
import kotlin.collections.ArrayList
import android.R.attr.data

import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil.DiffResult
import com.paypay.converter.interfaces.CurrencyInterface
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale





class ConversionListAdapter( private val context: Context, data: ArrayList<Currency>, listener: ConversionListAdapterClassAdapterListener) : RecyclerView.Adapter<RecyclerView.ViewHolder> (), Filterable{

    private val currentPos      = 0
    private val inflater        : LayoutInflater

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

        // Get current position of item in recyclerview to bind data and assign values from list
        val currencyListHolder                      = holder as CurrencyListHolder
        val currency                                = currencyListFiltered[position]

        val currencyName                            = currency.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        currencyListHolder.cell_currencySymbol.text = currency.symbol
        currencyListHolder.cell_currencyRateValue.text  = currency.rateValue.getCurrencyValueInLocale()
        currencyListHolder.cell_currencyName.text   = currencyName

        setAnimation(currencyListHolder.itemView, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {

        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val o = payloads[0] as Bundle
            for (key in o.keySet()) {

                if (key == "rateValue") {
                    //holder.icon.setText(data.get(position).getName().substring(0, 2))
                    //holder.name.setText(data.get(position).getName())}

                    val currencyListHolder                      = holder as CurrencyListHolder
                    val currency                                = currencyListFiltered[position]

                    val currencyName                            = currency.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    currencyListHolder.cell_currencySymbol.text = currency.symbol

                    //let's do some work on the rateValue
                    currencyListHolder.cell_currencyRateValue.text  = currency.rateValue.getCurrencyValueInLocale()
                    currencyListHolder.cell_currencyName.text       = currencyName
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

        var cell_currencySymbol: TextView
        var cell_currencyName: TextView
        var cell_currencyRateValue: TextView

        // create constructor to get widget reference
        init {
            cell_currencySymbol     = itemView.findViewById(R.id.cell_currencySymbol)
            cell_currencyName       = itemView.findViewById(R.id.cell_currencyName)
            cell_currencyRateValue  = itemView.findViewById(R.id.cell_currencyRateValue)

            itemView.setOnClickListener { // send selected contact in callback
                //listener.onItemSelected(currencyListFiltered[adapterPosition])
                listener.onConversionRateSelected(currencyListFiltered[bindingAdapterPosition])
            }
        }
    }

    init {
        inflater = LayoutInflater.from(context)
        currencyList = data
        currencyListFiltered = data
        this.listener = listener
    }


    class CurrencyComparator(var newList: ArrayList<Currency>, var oldList: ArrayList<Currency>) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return newList[newItemPosition].symbol == oldList.get(oldItemPosition).symbol
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val result = newList[newItemPosition].rateValue.compareTo(oldList[oldItemPosition].rateValue)
            return result == 0
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {

            /*
            val newCurrency: Currency = newList[newItemPosition]
            val oldCurrency: Currency = oldList[oldItemPosition]

            val diff = Bundle()
            if (!newCurrency.rateValue.equals(oldCurrency.rateValue)) {
                diff.putFloat("rateValue", newCurrency.rateValue)
            }

            return if (diff.size() == 0) {
                null
            } else diff
            */

            val newCurrency: Currency = newList[newItemPosition]
            val oldCurrency: Currency = oldList[oldItemPosition]

            if (oldCurrency.symbol == newCurrency.symbol) {
                return if (oldCurrency.rateValue == newCurrency.rateValue) {
                    super.getChangePayload(oldItemPosition, newItemPosition)
                } else {
                    val diff = Bundle()
                    diff.putFloat("rateValue", newCurrency.rateValue)
                    diff
                }
            }

            return super.getChangePayload(oldItemPosition, newItemPosition)

        }
    }
}

fun Float.getCurrencyValueInLocale(): String {
    val format = DecimalFormat("#,###,###.#######")
    val retObject = format.format(this)
    return retObject
}
