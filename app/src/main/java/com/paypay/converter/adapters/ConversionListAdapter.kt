package com.paypay.converter.adapters

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils

import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.paypay.converter.R
import com.paypay.converter.models.Currency

import java.util.*
import kotlin.collections.ArrayList

class ConversionListAdapter(
    private val context: Context,
    data: ArrayList<Currency>,
    listener: ConversionListAdapterClassAdapterListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val currentPos = 0
    private val inflater: LayoutInflater


    var currencyList: ArrayList<Currency> = ArrayList()
    var currencyListFiltered: ArrayList<Currency>? = ArrayList()
    var listener: ConversionListAdapterClassAdapterListener
    private var lastPosition = -1

    // Inflate the layout when viewholder created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = inflater.inflate(R.layout.cell_currency_list, parent, false)
        return currencyListHolder(view)
    }

    // Bind data
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        // Get current position of item in recyclerview to bind data and assign values from list
        val currencyListHolder                      = holder as currencyListHolder
        val currency                                = currencyListFiltered!![position]

        var currencyName                            = currency.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        currencyListHolder.cell_currencySymbol.text = (position + 1).toString()
        currencyListHolder.cell_currencyName.text   = currencyName

        //currencyListHolder.cell_currencyRateValue.setText(currency.rateValue)
        /*if (user.getAmountpaid() != null) {
            currencyListHolder.cell_beneficiary_amount.text =
                java.lang.String.format(
                    "%s %s",
                    context.getString(R.string.naira),
                    Utilities.getCurrencyFormat(user.getAmountpaid())
                )
        }*/
        setAnimation(currencyListHolder.itemView, position)
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
        return if (currencyListFiltered != null) currencyListFiltered!!.size else 0
    }

    /**
     * Notify.
     *
     * @param list the list
     */
    fun notify(list: ArrayList<Currency>) {
        currencyList            = list
        currencyListFiltered    = list
        notifyDataSetChanged()
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
                    val filteredList: ArrayList<Currency> = ArrayList<Currency>()
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
                currencyListFiltered = results.values as ArrayList<Currency>
                notifyDataSetChanged()
            }
        }
    }

    //Listener
    interface ConversionListAdapterClassAdapterListener {
        fun onItemSelected(currency: Currency?)
    }

    //For theViewHolder
    inner class currencyListHolder internal constructor(itemView: View) :
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
                listener.onItemSelected(currencyListFiltered!![adapterPosition])
            }
        }
    }

    init {
        inflater = LayoutInflater.from(context)
        currencyList = data
        currencyListFiltered = data
        this.listener = listener
    }
}