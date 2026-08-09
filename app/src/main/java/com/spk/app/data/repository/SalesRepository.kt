package com.spk.app.data.repository

import android.content.Context
import com.spk.app.data.db.AppDatabase
import com.spk.app.data.db.entity.CompletedWatchEntity
import com.spk.app.data.db.entity.SaleRecordEntity
import com.spk.app.data.db.entity.TrackedAccountEntity
import com.spk.app.data.db.entity.WatchedItemEntity
import com.spk.app.data.model.SaleDto
import com.spk.app.data.network.NetworkModule
import com.spk.app.util.PriceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NewSaleMatch(
    val itemName: String,
    val unitPrice: Long,
    val amount: Int,
    val accountName: String,
    val remainingAfter: Int,
    val wasCompleted: Boolean
)

class SalesRepository private constructor(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val watchedItemDao = db.watchedItemDao()
    private val trackedAccountDao = db.trackedAccountDao()
    private val saleRecordDao = db.saleRecordDao()
    private val completedWatchDao = db.completedWatchDao()
    private val settingsRepository = SettingsRepository.getInstance(context)
    private val api = NetworkModule.tradingPostApi

    fun observeWatchedItems() = watchedItemDao.observeAll()
    fun observeTrackedAccounts() = trackedAccountDao.observeAll()
    fun observeMatchedSales() = saleRecordDao.observeMatchedSales()
    fun observeCompletedWatches() = completedWatchDao.observeRecent()

    suspend fun watchItem(itemName: String, itemId: Int?, quantity: Int) = withContext(Dispatchers.IO) {
        // Baseline fetch: record existing sales as history but don't treat them as "new"
        val existing = fetchExact(itemName)
        val accounts = trackedAccountDao.getAll().map { it.name.lowercase() }.toSet()
        val records = existing.map { it.toEntity(accounts) }
        if (records.isNotEmpty()) saleRecordDao.insertAll(records)
        val maxId = existing.maxOfOrNull { it.id } ?: 0L
        watchedItemDao.upsert(
            WatchedItemEntity(
                itemName = itemName,
                itemId = itemId,
                addedAt = System.currentTimeMillis(),
                lastSeenSaleId = maxId,
                quantity = quantity.coerceAtLeast(1)
            )
        )
    }

    suspend fun unwatchItem(itemName: String) = withContext(Dispatchers.IO) {
        watchedItemDao.delete(itemName)
    }

    suspend fun isWatched(itemName: String): Boolean = withContext(Dispatchers.IO) {
        watchedItemDao.getByName(itemName) != null
    }

    suspend fun getWatchedQuantity(itemName: String): Int? = withContext(Dispatchers.IO) {
        watchedItemDao.getByName(itemName)?.quantity
    }

    /** Lets the user correct/refresh how many units they still have left to sell. */
    suspend fun updateQuantity(itemName: String, quantity: Int) = withContext(Dispatchers.IO) {
        watchedItemDao.updateQuantity(itemName, quantity.coerceAtLeast(0))
    }

    suspend fun addAccount(name: String) = withContext(Dispatchers.IO) {
        trackedAccountDao.upsert(TrackedAccountEntity(name = name.trim(), addedAt = System.currentTimeMillis()))
    }

    suspend fun removeAccount(name: String) = withContext(Dispatchers.IO) {
        trackedAccountDao.delete(name)
    }

    suspend fun fetchHistory(itemName: String): List<SaleDto> = withContext(Dispatchers.IO) {
        fetchExact(itemName)
    }

    suspend fun getStats(): Triple<Long, Int, Map<String, Pair<Long, Int>>> = withContext(Dispatchers.IO) {
        val matched = saleRecordDao.getMatchedSales()
        val totalValue = matched.sumOf { PriceUtils.totalPrice(it.price, it.currency, it.amount) }
        val totalCount = matched.size
        val perAccount = matched.groupBy { it.matchedAccount ?: "Unknown" }
            .mapValues { (_, sales) ->
                sales.sumOf { PriceUtils.totalPrice(it.price, it.currency, it.amount) } to sales.size
            }
        Triple(totalValue, totalCount, perAccount)
    }

    /**
     * Checks every watched item against the API, records new sales, decrements remaining
     * quantity for any sale that matched one of the user's accounts, and — once an item's
     * quantity hits zero — auto-archives it (if enabled) so the watchlist stays tidy while
     * the full sale history remains in sale_records for Stats.
     */
    suspend fun checkForNewSales(): List<NewSaleMatch> = withContext(Dispatchers.IO) {
        val watched = watchedItemDao.getAll()
        if (watched.isEmpty()) return@withContext emptyList()
        val accounts = trackedAccountDao.getAll().map { it.name.lowercase() }.toSet()
        if (accounts.isEmpty()) return@withContext emptyList()

        val autoRemove = settingsRepository.autoRemoveCompleted.value
        val matches = mutableListOf<NewSaleMatch>()

        for (watchedItem in watched) {
            val sales = fetchExact(watchedItem.itemName)
            if (sales.isEmpty()) continue

            val newSales = sales.filter { it.id > watchedItem.lastSeenSaleId }
            val newMax = maxOf(watchedItem.lastSeenSaleId, sales.maxOf { it.id })

            if (newSales.isEmpty()) {
                if (newMax != watchedItem.lastSeenSaleId) {
                    watchedItemDao.updateLastSeen(watchedItem.itemName, newMax)
                }
                continue
            }

            val entities = newSales.map { it.toEntity(accounts) }
            saleRecordDao.insertAll(entities)

            var remaining = watchedItem.quantity
            val matchedSales = newSales.filter { it.seller.lowercase() in accounts }

            for (sale in matchedSales) {
                remaining = (remaining - sale.amount).coerceAtLeast(0)
                val completed = remaining <= 0
                matches += NewSaleMatch(
                    itemName = sale.itemName,
                    unitPrice = PriceUtils.unitPrice(sale),
                    amount = sale.amount,
                    accountName = sale.seller,
                    remainingAfter = remaining,
                    wasCompleted = completed
                )
            }

            if (matchedSales.isNotEmpty() && remaining <= 0 && autoRemove) {
                completedWatchDao.insert(
                    CompletedWatchEntity(
                        itemName = watchedItem.itemName,
                        quantitySold = watchedItem.quantity,
                        completedAt = System.currentTimeMillis()
                    )
                )
                watchedItemDao.delete(watchedItem.itemName)
            } else {
                watchedItemDao.updateQuantityAndLastSeen(watchedItem.itemName, remaining, newMax)
            }
        }

        matches
    }

    /**
     * Queries the API and keeps only results whose item name exactly matches (case-insensitive)
     * the requested item. The API does a loose text search, so searching "armadyl godsword" can
     * also return "Armadyl godsword (or)" — this filters those variants back out.
     */
    private suspend fun fetchExact(itemName: String): List<SaleDto> {
        val results = runCatching { api.search(itemName, page = 1) }.getOrDefault(emptyList())
        return results.filter { it.itemName.equals(itemName, ignoreCase = true) }
    }

    private fun SaleDto.toEntity(trackedAccountsLower: Set<String>): SaleRecordEntity {
        val matched = if (seller.lowercase() in trackedAccountsLower) seller else null
        return SaleRecordEntity(
            id = id,
            itemName = itemName,
            itemId = itemId,
            time = time,
            amount = amount,
            price = price,
            currency = currency,
            seller = seller,
            buyer = buyer,
            matchedAccount = matched,
            fetchedAt = System.currentTimeMillis()
        )
    }

    companion object {
        @Volatile private var INSTANCE: SalesRepository? = null
        fun getInstance(context: Context): SalesRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SalesRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
