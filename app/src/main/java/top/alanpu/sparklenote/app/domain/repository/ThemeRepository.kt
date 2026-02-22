package top.alanpu.sparklenote.app.domain.repository

import top.alanpu.sparklenote.app.domain.model.Theme
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for theme data operations.
 * Defines the contract for theme management operations.
 */
interface ThemeRepository {
    
    /**
     * Create a new theme.
     * @param theme The theme to create
     * @return Result indicating success or failure
     */
    suspend fun createTheme(theme: Theme): Result<Unit>
    
    /**
     * Get all themes as a reactive stream.
     * @return Flow of list of all themes
     */
    fun getAllThemes(): Flow<List<Theme>>
    
    /**
     * Get a theme by name.
     * @param name The theme name
     * @return The theme or null if not found
     */
    suspend fun getThemeByName(name: String): Theme?
    
    /**
     * Update a theme.
     * @param theme The theme to update
     * @return Result indicating success or failure
     */
    suspend fun updateTheme(theme: Theme): Result<Unit>
    
    /**
     * Update theme name (cascades to inspirations).
     * @param oldName The current theme name
     * @param newName The new theme name
     * @return Result indicating success or failure
     */
    suspend fun updateThemeName(oldName: String, newName: String): Result<Unit>
    
    /**
     * Delete a theme.
     * @param name The theme name to delete
     * @param moveToTheme Optional theme to move inspirations to (default: "未分类")
     * @return Result indicating success or failure
     */
    suspend fun deleteTheme(name: String, moveToTheme: String = "未分类"): Result<Unit>
    
    /**
     * Check if a theme exists.
     * @param name The theme name to check
     * @return True if the theme exists
     */
    suspend fun themeExists(name: String): Boolean
    
    /**
     * Update theme last used timestamp.
     * @param name The theme name
     * @return Result indicating success or failure
     */
    suspend fun updateThemeLastUsed(name: String): Result<Unit>
    
    /**
     * Get themes ordered by last used (most recent first).
     * @return Flow of list of themes
     */
    fun getThemesByLastUsed(): Flow<List<Theme>>
    
    /**
     * Get themes ordered by inspiration count (highest first).
     * @return Flow of list of themes
     */
    fun getThemesByInspirationCount(): Flow<List<Theme>>
    
    /**
     * Refresh theme inspiration counts.
     * Updates the cached count for all themes.
     * @return Result indicating success or failure
     */
    suspend fun refreshThemeCounts(): Result<Unit>
}