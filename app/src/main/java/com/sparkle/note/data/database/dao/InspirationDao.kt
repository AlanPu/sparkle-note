package com.sparkle.note.data.database.dao

import androidx.room.*
import com.sparkle.note.data.entity.InspirationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for inspiration operations.
 * Provides optimized queries for theme management and search functionality.
 */
@Dao
interface InspirationDao {
    
    /**
     * Inserts a new inspiration into the database.
     * @return The ID of the inserted inspiration
     */
    @Insert
    suspend fun insert(inspiration: InspirationEntity): Long
    
    /**
     * Retrieves all inspirations ordered by creation date (newest first).
     */
    @Query("SELECT * FROM inspirations ORDER BY created_at DESC")
    fun getAllInspirations(): Flow<List<InspirationEntity>>
    
    /**
     * Gets distinct theme names from all inspirations.
     */
    @Query("SELECT DISTINCT theme_name FROM inspirations ORDER BY theme_name")
    fun getDistinctThemes(): Flow<List<String>>
    
    /**
     * Searches inspirations by keyword in content and theme name.
     * @param keyword The search keyword
     */
    @Query("""
        SELECT * FROM inspirations 
        WHERE content LIKE '%' || :keyword || '%' 
           OR theme_name LIKE '%' || :keyword || '%'
        ORDER BY created_at DESC
    """)
    fun searchInspirations(keyword: String): Flow<List<InspirationEntity>>
    
    /**
     * Gets inspirations filtered by theme name.
     * @param themeName The theme to filter by
     */
    @Query("SELECT * FROM inspirations WHERE theme_name = :themeName ORDER BY created_at DESC")
    fun getInspirationsByTheme(themeName: String): Flow<List<InspirationEntity>>
    
    /**
     * Retrieves a single inspiration by ID.
     * @param id The inspiration ID
     */
    @Query("SELECT * FROM inspirations WHERE id = :id")
    suspend fun getInspirationById(id: Long): InspirationEntity?
    
    /**
     * Deletes an inspiration by ID.
     * @param id The ID of the inspiration to delete
     */
    @Query("DELETE FROM inspirations WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    /**
     * Updates an existing inspiration.
     * @param inspiration The inspiration to update
     */
    @Update
    suspend fun update(inspiration: InspirationEntity)
    
    /**
     * Gets the total count of inspirations.
     */
    @Query("SELECT COUNT(*) FROM inspirations")
    suspend fun getInspirationCount(): Long
    
    /**
     * Gets the count of inspirations for a specific theme.
     * @param themeName The theme name to count
     */
    @Query("SELECT COUNT(*) FROM inspirations WHERE theme_name = :themeName")
    suspend fun getInspirationCountByTheme(themeName: String): Long
}