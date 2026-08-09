package com.spk.app.util

import com.spk.app.data.model.SaleDto
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Handles the trading-post currency quirk and human-readable price formatting.
 *
 * When `currency == 1` the raw `price` field is in "100 million gp" units *per unit item*,
 * not a total. Every other currency value is treated as a plain gp price already.
 */
object PriceUtils {

    private const val CURRENCY_100M_UNIT = 1L
    private const val HUNDRED_MILLION = 100_000_000L

    /** Price for a single unit of the item, in real gp. */
    fun unitPrice(price: Long, currency: Int): Long =
        if (currency == CURRENCY_100M_UNIT.toInt()) price * HUNDRED_MILLION else price

    /** Total price for the whole sale (unit price * amount), in real gp. */
    fun totalPrice(price: Long, currency: Int, amount: Int): Long =
        unitPrice(price, currency) * amount

    fun unitPrice(sale: SaleDto): Long = unitPrice(sale.price, sale.currency)
    fun totalPrice(sale: SaleDto): Long = totalPrice(sale.price, sale.currency, sale.amount)

    /**
     * Formats gp values as 100k, 100.5k, 100m, 100.6m, 100b, 100.6b, 100t, 100.6t, 100q, 100.2q.
     * Values under 1,000 are shown as a plain number. Trailing ".0" is dropped.
     */
    fun format(value: Long): String {
        val negative = value < 0
        val abs = kotlin.math.abs(value)

        val (divisor, suffix) = when {
            abs >= 1_000_000_000_000_000L -> 1_000_000_000_000_000L to "q"
            abs >= 1_000_000_000_000L -> 1_000_000_000_000L to "t"
            abs >= 1_000_000_000L -> 1_000_000_000L to "b"
            abs >= 1_000_000L -> 1_000_000L to "m"
            abs >= 1_000L -> 1_000L to "k"
            else -> 1L to ""
        }

        val text = if (divisor == 1L) {
            abs.toString()
        } else {
            val scaled = BigDecimal(abs).divide(BigDecimal(divisor), 4, RoundingMode.DOWN)
                .setScale(1, RoundingMode.DOWN)
            var s = scaled.toPlainString()
            if (s.endsWith(".0")) s = s.dropLast(2)
            s
        }

        return (if (negative) "-" else "") + text + suffix
    }
}
