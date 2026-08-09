package com.spk.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A small trace left behind when a watched item is auto-removed after being fully sold.
 * The full sale history stays in sale_records regardless (that's what Stats reads from) —
 * this table just remembers *that* a watch completed, for a "recently completed" list.
 */
@Entity(tableName = "completed_watches")
data class CompletedWatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemName: String,
    val quantitySold: Int,
    val completedAt: Long
)
