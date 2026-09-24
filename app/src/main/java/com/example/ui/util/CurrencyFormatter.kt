package com.example.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val idSymbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val numberFormat = DecimalFormat("#,###", idSymbols)

    fun format(amount: Double, symbol: String = "Rp"): String {
        val formattedNum = numberFormat.format(amount)
        return if (symbol.trim().equals("rp", ignoreCase = true)) {
            "Rp $formattedNum"
        } else {
            "$symbol $formattedNum"
        }
    }

    fun formatRupiah(amount: Double): String {
        return format(amount, "Rp")
    }

    fun formatPlain(amount: Double): String {
        return numberFormat.format(amount)
    }
}
