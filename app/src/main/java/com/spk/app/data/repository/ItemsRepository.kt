package com.spk.app.data.repository

import android.content.Context
import com.spk.app.AppConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

sealed class ItemsLoadState {
    object Idle : ItemsLoadState()
    data class Loading(val message: String) : ItemsLoadState()
    data class Ready(val itemCount: Int, val updatedFromRemote: Boolean) : ItemsLoadState()
    data class Error(val message: String) : ItemsLoadState()
}

/**
 * A single searchable item: cleaned display/search name -> game item id.
 */
data class MarketItem(val name: String, val itemId: Int)

class ItemsRepository private constructor(private val appContext: Context) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)
    private val adapter = moshi.adapter<Map<String, Int>>(mapType)

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val cacheFile: File get() = File(appContext.filesDir, "items.json")

    private val _loadState = MutableStateFlow<ItemsLoadState>(ItemsLoadState.Idle)
    val loadState: StateFlow<ItemsLoadState> = _loadState.asStateFlow()

    private val _items = MutableStateFlow<List<MarketItem>>(emptyList())
    val items: StateFlow<List<MarketItem>> = _items.asStateFlow()

    /**
     * Loads items for use right away (bundled asset or existing cache),
     * then tries to refresh from GitHub in the background and updates
     * [items]/[loadState] again if the remote copy is different.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        _loadState.value = ItemsLoadState.Loading("Loading item list…")

        val localRaw = when {
            cacheFile.exists() -> runCatching { cacheFile.readText() }.getOrNull()
            else -> runCatching {
                appContext.assets.open("items.json").bufferedReader().use { it.readText() }
            }.getOrNull()
        }

        var raw = localRaw
        if (raw != null) {
            publish(raw, updatedFromRemote = false)
        }

        _loadState.value = ItemsLoadState.Loading("Checking for item list updates…")
        val remoteRaw = fetchRemote()
        if (remoteRaw != null && remoteRaw != raw) {
            runCatching { cacheFile.writeText(remoteRaw) }
            publish(remoteRaw, updatedFromRemote = true)
            raw = remoteRaw
        } else if (raw == null) {
            _loadState.value = ItemsLoadState.Error("Couldn't load the item list. Check your connection.")
            return@withContext
        }
    }

    private fun publish(raw: String, updatedFromRemote: Boolean) {
        val parsed = runCatching { adapter.fromJson(raw) }.getOrNull() ?: emptyMap()
        val cleaned = parsed.entries
            .filter { it.key.startsWith(AppConfig.VALID_ITEM_PREFIX) }
            .map { MarketItem(name = it.key.removePrefix(AppConfig.VALID_ITEM_PREFIX), itemId = it.value) }
            .sortedBy { it.name }
        _items.value = cleaned
        _loadState.value = ItemsLoadState.Ready(itemCount = cleaned.size, updatedFromRemote = updatedFromRemote)
    }

    private fun fetchRemote(): String? {
        return runCatching {
            val request = Request.Builder().url(AppConfig.ITEMS_JSON_RAW_URL).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        }.getOrNull()
    }

    fun search(query: String, limit: Int = 60): List<MarketItem> {
        if (query.isBlank()) return _items.value.take(limit)
        val q = query.trim().lowercase()
        return _items.value
            .filter { it.name.lowercase().contains(q) }
            .take(limit)
    }

    fun findByName(name: String): MarketItem? = _items.value.firstOrNull { it.name.equals(name, ignoreCase = true) }

    companion object {
        @Volatile private var INSTANCE: ItemsRepository? = null
        fun getInstance(context: Context): ItemsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ItemsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
