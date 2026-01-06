package com.sparkle.note.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Theme manager for handling theme preferences and settings.
 * Uses DataStore for persistent storage of theme preferences.
 */
@Singleton
class ThemeManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val THEME_PREFERENCES_NAME = "theme_preferences"
        private val THEME_STYLE_KEY = stringPreferencesKey("theme_style")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        
        // Extension property for DataStore
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = THEME_PREFERENCES_NAME
        )
    }

    /**
     * Get the current theme style as a Flow
     */
    val themeStyle: Flow<ThemeStyle> = context.dataStore.data
        .map { preferences ->
            val themeStyleName = preferences[THEME_STYLE_KEY] ?: ThemeStyle.NORDIC.name
            try {
                ThemeStyle.valueOf(themeStyleName)
            } catch (e: IllegalArgumentException) {
                ThemeStyle.NORDIC
            }
        }

    /**
     * Get the current dark mode preference as a Flow
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: true // Default to dark mode
        }

    /**
     * Set the theme style
     */
    suspend fun setThemeStyle(themeStyle: ThemeStyle) {
        context.dataStore.edit { preferences ->
            preferences[THEME_STYLE_KEY] = themeStyle.name
        }
    }

    /**
     * Set dark mode preference
     */
    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }

    /**
     * Get theme style name for display
     */
    fun getThemeStyleName(themeStyle: ThemeStyle): String {
        return when (themeStyle) {
            ThemeStyle.NORDIC -> "北欧风格"
            ThemeStyle.DEEP_NIGHT -> "深邃夜空"
            ThemeStyle.MINT_MORNING -> "薄荷晨露"
            ThemeStyle.SCHOLAR_BLUE -> "学院蓝调"
        }
    }

    /**
     * Get theme description
     */
    fun getThemeDescription(themeStyle: ThemeStyle): String {
        return when (themeStyle) {
            ThemeStyle.NORDIC -> "经典的北欧简约设计"
            ThemeStyle.DEEP_NIGHT -> "纯黑背景配霓虹蓝绿点缀"
            ThemeStyle.MINT_MORNING -> "清新薄荷绿配柔和色调"
            ThemeStyle.SCHOLAR_BLUE -> "知性深蓝配经典学术风"
        }
    }

    /**
     * Get all available theme styles
     */
    fun getAllThemeStyles(): List<ThemeStyle> {
        return ThemeStyle.values().toList()
    }
}