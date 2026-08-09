package com.spk.app.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository private constructor(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("market_pulse_settings", Context.MODE_PRIVATE)

    private val _autoRemoveCompleted = MutableStateFlow(prefs.getBoolean(KEY_AUTO_REMOVE, true))
    val autoRemoveCompleted: StateFlow<Boolean> = _autoRemoveCompleted.asStateFlow()

    fun setAutoRemoveCompleted(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_REMOVE, enabled).apply()
        _autoRemoveCompleted.value = enabled
    }

    companion object {
        private const val KEY_AUTO_REMOVE = "auto_remove_completed"

        @Volatile private var INSTANCE: SettingsRepository? = null
        fun getInstance(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
