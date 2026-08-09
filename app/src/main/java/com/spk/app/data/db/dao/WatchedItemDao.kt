package com.spk.app.data.db.dao

import androidx.room.*
import com.spk.app.data.db.entity.WatchedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedItemDao {
    @Query("SELECT * FROM watched_items ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<WatchedItemEntity>>

    @Query("SELECT * FROM watched_items")
    suspend fun getAll(): List<WatchedItemEntity>

    @Query("SELECT * FROM watched_items WHERE itemName = :itemName LIMIT 1")
    suspend fun getByName(itemName: String): WatchedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WatchedItemEntity)

    @Query("DELETE FROM watched_items WHERE itemName = :itemName")
    suspend fun delete(itemName: String)

    @Query("UPDATE watched_items SET lastSeenSaleId = :lastSeenSaleId WHERE itemName = :itemName")
    suspend fun updateLastSeen(itemName: String, lastSeenSaleId: Long)

    @Query("UPDATE watched_items SET quantity = :quantity WHERE itemName = :itemName")
    suspend fun updateQuantity(itemName: String, quantity: Int)

    @Query("UPDATE watched_items SET quantity = :quantity, lastSeenSaleId = :lastSeenSaleId WHERE itemName = :itemName")
    suspend fun updateQuantityAndLastSeen(itemName: String, quantity: Int, lastSeenSaleId: Long)
}
