package com.spk.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watched_items")
data class WatchedItemEntity(
    @PrimaryKey val itemName: String,
    val itemId: Int?,
    val addedAt: Long,
    val lastSeenSaleId: Long = 0L,
    /** How many units are still left to sell. Decremented automatically as matched sales come in. */
    val quantity: Int = 1
)
