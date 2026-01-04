package com.sparkle.note.data.repository

import com.sparkle.note.domain.model.Inspiration
import java.util.concurrent.atomic.AtomicLong

/**
 * Mock implementation of InspirationRepository for testing and demo purposes.
 * Provides in-memory data storage with realistic behavior.
 */
class MockInspirationRepository : com.sparkle.note.domain.repository.InspirationRepository {
    
    private val inspirations = mutableListOf<Inspiration>()
    private val idCounter = AtomicLong(1L)
    
    override suspend fun saveInspiration(inspiration: Inspiration): Result<Unit> {
        return try {
            val newInspiration = if (inspiration.id == 0L) {
                inspiration.copy(id = idCounter.incrementAndGet())
            } else {
                inspiration
            }
            inspirations.add(newInspiration)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllInspirations(): kotlinx.coroutines.flow.Flow<List<Inspiration>> {
        return kotlinx.coroutines.flow.flow {
            emit(inspirations.toList())
        }
    }
    
    override fun getDistinctThemes(): kotlinx.coroutines.flow.Flow<List<String>> {
        return kotlinx.coroutines.flow.flow {
            emit(inspirations.map { it.themeName }.distinct())
        }
    }
    
    override fun searchInspirations(keyword: String): kotlinx.coroutines.flow.Flow<List<Inspiration>> {
        return kotlinx.coroutines.flow.flow {
            val filtered = inspirations.filter { 
                it.content.contains(keyword, ignoreCase = true) || 
                it.themeName.contains(keyword, ignoreCase = true)
            }
            emit(filtered)
        }
    }
    
    override fun getInspirationsByTheme(themeName: String): kotlinx.coroutines.flow.Flow<List<Inspiration>> {
        return kotlinx.coroutines.flow.flow {
            emit(inspirations.filter { it.themeName == themeName })
        }
    }
    
    override suspend fun deleteInspiration(id: Long): Result<Unit> {
        return try {
            inspirations.removeIf { it.id == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun exportToMarkdown(inspirations: List<Inspiration>): String {
        return com.sparkle.note.utils.ExportManager.exportBatchToMarkdown(inspirations)
    }
    
    override fun exportSingleToMarkdown(inspiration: Inspiration): String {
        return com.sparkle.note.utils.ExportManager.exportSingleToMarkdown(inspiration)
    }
}