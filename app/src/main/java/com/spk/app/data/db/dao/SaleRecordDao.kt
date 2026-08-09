package com.spk.app.data.db.dao

import androidx.room.*
import com.spk.app.data.db.entity.SaleRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleRecordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(records: List<SaleRecordEntity>)

    @Query("SELECT * FROM sale_records WHERE itemName = :itemName ORDER BY id DESC LIMIT :limit")
    suspend fun getHistoryForItem(itemName: String, limit: Int = 100): List<SaleRecordEntity>

    @Query("SELECT * FROM sale_records WHERE matchedAccount IS NOT NULL ORDER BY id DESC")
    fun observeMatchedSales(): Flow<List<SaleRecordEntity>>

    @Query("SELECT * FROM sale_records WHERE matchedAccount IS NOT NULL ORDER BY id DESC")
    suspend fun getMatchedSales(): List<SaleRecordEntity>

    @Query("SELECT COUNT(*) FROM sale_records WHERE matchedAccount IS NOT NULL")
    suspend fun getMatchedSalesCount(): Int

    @Query("SELECT SUM(price * amount) FROM sale_records WHERE matchedAccount IS NOT NULL")
    suspend fun getTotalMatchedValue(): Long?
}
