package com.spk.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_records")
data class SaleRecordEntity(
    @PrimaryKey val id: Long,
    val itemName: String,
    val itemId: Int,
    val time: String,
    val amount: Int,
    val price: Long,
    val currency: Int,
    val seller: String,
    val buyer: String,
    // non-null when `seller` matched one of the user's tracked account names
    val matchedAccount: String?,
    val fetchedAt: Long
)
