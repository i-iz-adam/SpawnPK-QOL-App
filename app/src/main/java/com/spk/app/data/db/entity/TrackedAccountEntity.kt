package com.spk.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_accounts")
data class TrackedAccountEntity(
    @PrimaryKey val name: String,
    val addedAt: Long
)
