package com.spk.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SaleDto(
    val id: Long,
    val time: String,
    @Json(name = "item_id") val itemId: Int,
    @Json(name = "item_name") val itemName: String,
    val amount: Int,
    val price: Long,
    val currency: Int,
    val seller: String,
    val buyer: String
)
