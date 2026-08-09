package com.spk.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.spk.app.data.db.entity.CompletedWatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedWatchDao {
    @Insert
    suspend fun insert(entity: CompletedWatchEntity)

    @Query("SELECT * FROM completed_watches ORDER BY completedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<CompletedWatchEntity>>
}
