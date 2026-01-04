package com.sparkle.note.domain.repository

import com.sparkle.note.domain.model.Inspiration
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for inspiration data operations.
 * Defines the contract for data layer operations.
 */
interface InspirationRepository {
    
    /**
     * Save a new inspiration to the database.
     * @param inspiration The inspiration to save
     * @return Result indicating success or failure
     */
    suspend fun saveInspiration(inspiration: Inspiration): Result<Unit>
    
    /**
     * Get all inspirations as a reactive stream.
     * @return Flow of list of all inspirations
     */
    fun getAllInspirations(): Flow<List<Inspiration>>
    
    /**
     * Get distinct theme names.
     * @return Flow of list of unique theme names
     */
    fun getDistinctThemes(): Flow<List<String>>
    
    /**
     * Search inspirations by keyword.
     * @param keyword The search keyword
     * @return Flow of list of matching inspirations
     */
    fun searchInspirations(keyword: String): Flow<List<Inspiration>>
    
    /**
     * Get inspirations by specific theme.
     * @param themeName The theme name to filter by
     * @return Flow of list of inspirations for the theme
     */
    fun getInspirationsByTheme(themeName: String): Flow<List<Inspiration>>
    
    /**
     * Delete an inspiration by ID.
     * @param id The ID of the inspiration to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteInspiration(id: Long): Result<Unit>
    
    /**
     * Export inspirations to markdown format.
     * @param inspirations List of inspirations to export
     * @return Markdown formatted string
     */
    fun exportToMarkdown(inspirations: List<Inspiration>): String
    
    /**
     * Export single inspiration to markdown format.
     * @param inspiration The inspiration to export
     * @return Markdown formatted string
     */
    fun exportSingleToMarkdown(inspiration: Inspiration): String
}