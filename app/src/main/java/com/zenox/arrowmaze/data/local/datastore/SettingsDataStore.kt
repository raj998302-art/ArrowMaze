package com.zenox.arrowmaze.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val COLOR_BLIND_MODE = booleanPreferencesKey("color_blind_mode")
        val LARGE_TEXT = booleanPreferencesKey("large_text")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val CURRENT_THEME = stringPreferencesKey("current_theme")
    }

    // --- Flow getters ---

    val musicEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.MUSIC_ENABLED] ?: true }
    val sfxEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.SFX_ENABLED] ?: true }
    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.VIBRATION_ENABLED] ?: true }
    val colorBlindMode: Flow<Boolean> = dataStore.data.map { it[Keys.COLOR_BLIND_MODE] ?: false }
    val largeText: Flow<Boolean> = dataStore.data.map { it[Keys.LARGE_TEXT] ?: false }
    val musicVolume: Flow<Float> = dataStore.data.map { it[Keys.MUSIC_VOLUME] ?: 0.7f }
    val sfxVolume: Flow<Float> = dataStore.data.map { it[Keys.SFX_VOLUME] ?: 0.8f }
    val darkMode: Flow<Boolean> = dataStore.data.map { it[Keys.DARK_MODE] ?: false }
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val currentTheme: Flow<String> = dataStore.data.map { it[Keys.CURRENT_THEME] ?: "light" }

    // --- Suspend setters ---

    suspend fun setMusicEnabled(value: Boolean) {
        dataStore.edit { it[Keys.MUSIC_ENABLED] = value }
    }

    suspend fun setSfxEnabled(value: Boolean) {
        dataStore.edit { it[Keys.SFX_ENABLED] = value }
    }

    suspend fun setVibrationEnabled(value: Boolean) {
        dataStore.edit { it[Keys.VIBRATION_ENABLED] = value }
    }

    suspend fun setColorBlindMode(value: Boolean) {
        dataStore.edit { it[Keys.COLOR_BLIND_MODE] = value }
    }

    suspend fun setLargeText(value: Boolean) {
        dataStore.edit { it[Keys.LARGE_TEXT] = value }
    }

    suspend fun setMusicVolume(value: Float) {
        dataStore.edit { it[Keys.MUSIC_VOLUME] = value }
    }

    suspend fun setSfxVolume(value: Float) {
        dataStore.edit { it[Keys.SFX_VOLUME] = value }
    }

    suspend fun setDarkMode(value: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = value }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = value }
    }

    suspend fun setCurrentTheme(value: String) {
        dataStore.edit { it[Keys.CURRENT_THEME] = value }
    }
}