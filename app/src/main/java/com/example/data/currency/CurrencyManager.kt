package com.example.data.currency

import java.text.NumberFormat
import java.util.Locale

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val flagEmoji: String,
    val ratePerUsd: Double // How many units of this currency equal 1 USD
)

object CurrencyManager {
    val SUPPORTED_CURRENCIES: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "$", "US Dollar", "🇺🇸", 1.0),
        CurrencyInfo("INR", "₹", "Indian Rupee", "🇮🇳", 83.5),
        CurrencyInfo("EUR", "€", "Euro", "🇪🇺", 0.92),
        CurrencyInfo("GBP", "£", "British Pound", "🇬🇧", 0.79),
        CurrencyInfo("RON", "lei", "Romanian Leu", "🇷🇴", 4.58),
        CurrencyInfo("AED", "AED", "UAE Dirham", "🇦🇪", 3.67),
        CurrencyInfo("SAR", "SAR", "Saudi Riyal", "🇸🇦", 3.75),
        CurrencyInfo("BDT", "৳", "Bangladeshi Taka", "🇧🇩", 117.2),
        CurrencyInfo("PKR", "₨", "Pakistani Rupee", "🇵🇰", 278.5),
        CurrencyInfo("NPR", "रू", "Nepalese Rupee", "🇳🇵", 133.6),
        CurrencyInfo("CAD", "C$", "Canadian Dollar", "🇨🇦", 1.37),
        CurrencyInfo("AUD", "A$", "Australian Dollar", "🇦🇺", 1.52),
        CurrencyInfo("JPY", "¥", "Japanese Yen", "🇯🇵", 155.0),
        CurrencyInfo("TRY", "₺", "Turkish Lira", "🇹🇷", 32.8),
        CurrencyInfo("BRL", "R$", "Brazilian Real", "🇧🇷", 5.45),
        CurrencyInfo("PHP", "₱", "Philippine Peso", "🇵🇭", 58.2),
        CurrencyInfo("IDR", "Rp", "Indonesian Rupiah", "🇮🇩", 16250.0),
        CurrencyInfo("MYR", "RM", "Malaysian Ringgit", "🇲🇾", 4.71),
        CurrencyInfo("THB", "฿", "Thai Baht", "🇹🇭", 36.7),
        CurrencyInfo("CNY", "¥", "Chinese Yuan", "🇨🇳", 7.24)
    )

    private val currencyMap = SUPPORTED_CURRENCIES.associateBy { it.code.uppercase() }

    fun getCurrency(code: String): CurrencyInfo {
        return currencyMap[code.uppercase()] ?: CurrencyInfo(code, code, code, "🌐", 1.0)
    }

    /**
     * Converts an amount from source currency to target currency.
     * Never assumes 1:1 parity between different currencies.
     */
    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        if (fromCode.equals(toCode, ignoreCase = true)) return amount
        val fromRate = getCurrency(fromCode).ratePerUsd
        val toRate = getCurrency(toCode).ratePerUsd
        if (fromRate <= 0.0) return amount
        val inUsd = amount / fromRate
        return inUsd * toRate
    }

    /**
     * Format a price with proper symbol and decimal places
     */
    fun formatPrice(amount: Double, currencyCode: String): String {
        val curr = getCurrency(currencyCode)
        val formattedNumber = if (curr.code in listOf("JPY", "IDR", "KRW", "VND")) {
            NumberFormat.getIntegerInstance(Locale.US).format(amount.toLong())
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
        return if (curr.symbol == curr.code) {
            "$formattedNumber ${curr.code}"
        } else {
            "${curr.symbol}$formattedNumber"
        }
    }

    /**
     * Returns a clear presentation distinguishing original listing price from buyer's viewing currency
     */
    fun formatDisplayPrice(
        originalPrice: Double,
        originalCurrency: String,
        userCurrency: String
    ): PriceDisplay {
        val originalFormatted = formatPrice(originalPrice, originalCurrency)
        if (originalCurrency.equals(userCurrency, ignoreCase = true)) {
            return PriceDisplay(
                primaryPrice = "$originalFormatted $originalCurrency",
                secondaryPrice = null,
                isConverted = false,
                originalCurrency = originalCurrency
            )
        }
        val convertedAmount = convert(originalPrice, originalCurrency, userCurrency)
        val convertedFormatted = formatPrice(convertedAmount, userCurrency)
        return PriceDisplay(
            primaryPrice = "$convertedFormatted $userCurrency",
            secondaryPrice = "(Original: $originalFormatted $originalCurrency)",
            isConverted = true,
            originalCurrency = originalCurrency
        )
    }
}

data class PriceDisplay(
    val primaryPrice: String,
    val secondaryPrice: String?,
    val isConverted: Boolean,
    val originalCurrency: String
)
