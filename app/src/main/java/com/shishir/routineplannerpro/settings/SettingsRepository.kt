package com.shishir.routineplannerpro.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "routine_planner_settings")

class SettingsRepository(private val context: Context) {
    private val themeDarkKey = booleanPreferencesKey("theme_dark")
    private val openRouterApiKey = stringPreferencesKey("openrouter_api_key")

    val isDarkTheme: Flow<Boolean> = context.settingsStore.data.map { it[themeDarkKey] ?: false }
    val apiKey: Flow<String> = context.settingsStore.data.map { it[openRouterApiKey] ?: "" }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.settingsStore.edit { prefs -> prefs[themeDarkKey] = enabled }
    }

    suspend fun saveApiKey(key: String) {
        context.settingsStore.edit { prefs -> prefs[openRouterApiKey] = key.trim() }
    }

    suspend fun deleteApiKey() {
        context.settingsStore.edit { prefs -> prefs.remove(openRouterApiKey) }
    }

    fun maskKey(value: String): String {
        if (value.isBlank()) return "Not set"
        return "*".repeat(value.length.coerceAtLeast(8))
    }
}
