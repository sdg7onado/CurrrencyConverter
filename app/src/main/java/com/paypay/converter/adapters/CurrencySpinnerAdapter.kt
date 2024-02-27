/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.paypay.converter.R
import com.paypay.converter.models.Currency

class CurrencySpinnerAdapter(private val context: Context, currencies: List<Currency>) : BaseAdapter() {

    private val inflater: LayoutInflater
    private var currencies: List<Currency>

    override fun getCount(): Int {
        return currencies.size
    }

    override fun getItem(i: Int): Currency {
        return currencies[i]
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    fun notify(list: List<Currency>) {
        currencies = list
        notifyDataSetChanged()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {

        var view        = view
        val viewHolder  : ViewHolder

        if (view == null) {
            view                                = LayoutInflater.from(context).inflate(R.layout.currency_spinner_items, viewGroup, false)
            viewHolder                          = ViewHolder()
            viewHolder.spinner_currency_label   = view.findViewById(R.id.spinner_currency_label)
            view.setTag(viewHolder)

        } else {
            viewHolder                          = view.tag as ViewHolder
        }

        viewHolder.spinner_currency_label.text =
            context.getString( R.string.spinner_currency_label,currencies[i].symbol,currencies[i].name)

        return view!!
    }

    inner class ViewHolder {

        lateinit var spinner_currency_label: TextView
    }


    //Listener
    interface CurrencySpinnerAdapterListener {
        fun onCurrencySelected(selectedCurrency: Currency)
    }

    init {
        this.currencies = currencies
        inflater = LayoutInflater.from(context)
    }
}