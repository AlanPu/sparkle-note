package com.sparkle.note.data.database.dao

import androidx.room.*
import com.sparkle.note.data.entity.ThemeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for theme operations.
 * Provides complete CRUD operations for theme management.
 */
@Dao
interface ThemeDao {
    
    /**
     * Inserts a new theme into the database.
     * @param theme The theme to insert
     * @return The ID of the inserted theme (always the name)
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(theme: ThemeEntity): Long
    
    /**
     * Updates an existing theme.
     * @param theme The theme to update
     */
    @Update
    suspend fun update(theme: ThemeEntity)
    
    /**
     * Deletes a theme by entity.
     * @param theme The theme to delete
     */
    @Delete
    suspend fun delete(theme: ThemeEntity)
    
    /**
     * Retrieves all themes ordered by name.
     */
    @Query("SELECT * FROM themes ORDER BY name")
    fun getAllThemes(): Flow<List<ThemeEntity>>
    
    /**
     * Gets a theme by name.
     * @param name The theme name
     */
    @Query("SELECT * FROM themes WHERE name = :name")
    suspend fun getThemeByName(name: String): ThemeEntity?
    
    /**
     * Gets a theme by name as Flow.
     * @param name The theme name
     */
    @Query("SELECT * FROM themes WHERE name = :name")
    fun getThemeByNameFlow(name: String): Flow<ThemeEntity?>
    
    /**
     * Updates theme name (handles cascade updates via foreign key).
     * @param oldName The current theme name
     * @param newName The new theme name
     */
    @Query("UPDATE themes SET name = :newName WHERE name = :oldName")
    suspend fun updateThemeName(oldName: String, newName: String)
    
    /**
     * Updates theme last used timestamp.
     * @param name The theme name
     * @param lastUsed The new timestamp
     */
    @Query("UPDATE themes SET lastUsed = :lastUsed WHERE name = :name")
    suspend fun updateThemeLastUsed(name: String, lastUsed: Long)
    
    /**
     * Updates theme inspiration count.
     * @param name The theme name
     * @param count The new count
     */
    @Query("UPDATE themes SET inspirationCount = :count WHERE name = :name")
    suspend fun updateThemeInspirationCount(name: String, count: Int)
    
    /**
     * Gets the total count of themes.
     */
    @Query("SELECT COUNT(*) FROM themes")
    suspend fun getThemeCount(): Long
    
    /**
     * Checks if a theme exists.
     * @param name The theme name to check
     */
    @Query("SELECT EXISTS(SELECT 1 FROM themes WHERE name = :name)")
    suspend fun themeExists(name: String): Boolean
    
    /**
     * Gets themes ordered by last used (most recent first).
     */
    @Query("SELECT * FROM themes ORDER BY lastUsed DESC")
    fun getThemesByLastUsed(): Flow<List<ThemeEntity>>
    
    /**
     * Gets themes ordered by inspiration count (highest first).
     */
    @Query("SELECT * FROM themes ORDER BY inspirationCount DESC")
    fun getThemesByInspirationCount(): Flow<List<ThemeEntity>>
}