package com.spk.app.data.db.dao

import androidx.room.*
import com.spk.app.data.db.entity.TrackedAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedAccountDao {
    @Query("SELECT * FROM tracked_accounts ORDER BY addedAt ASC")
    fun observeAll(): Flow<List<TrackedAccountEntity>>

    @Query("SELECT * FROM tracked_accounts")
    suspend fun getAll(): List<TrackedAccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: TrackedAccountEntity)

    @Query("DELETE FROM tracked_accounts WHERE name = :name")
    suspend fun delete(name: String)
}
