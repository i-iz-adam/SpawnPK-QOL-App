package com.spk.app.data.network

import com.spk.app.data.model.SaleDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TradingPostApi {
    @GET("tradingpost")
    suspend fun search(
        @Query("search_text") searchText: String,
        @Query("page") page: Int = 1
    ): List<SaleDto>
}
