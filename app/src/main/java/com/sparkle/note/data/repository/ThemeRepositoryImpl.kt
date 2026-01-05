package com.sparkle.note.data.repository

import com.sparkle.note.data.database.dao.ThemeDao
import com.sparkle.note.data.database.dao.InspirationDao
import com.sparkle.note.data.entity.ThemeEntity
import com.sparkle.note.domain.model.Theme
import com.sparkle.note.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of ThemeRepository using Room database.
 * Provides complete theme management with data integrity.
 */
class ThemeRepositoryImpl @Inject constructor(
    private val themeDao: ThemeDao,
    private val inspirationDao: InspirationDao
) : ThemeRepository {
    
    override suspend fun createTheme(theme: Theme): Result<Unit> {
        return try {
            // Validate theme name
            when (theme.validateName()) {
                com.sparkle.note.domain.model.ValidationResult.Valid -> {
                    val entity = theme.toEntity()
                    themeDao.insert(entity)
                    Result.success(Unit)
                }
                com.sparkle.note.domain.model.ValidationResult.Empty -> {
                    Result.failure(Exception("Theme name cannot be empty"))
                }
                com.sparkle.note.domain.model.ValidationResult.TooLong -> {
                    Result.failure(Exception("Theme name is too long"))
                }
                is com.sparkle.note.domain.model.ValidationResult.Invalid -> {
                    Result.failure(Exception("Theme name contains invalid characters"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllThemes(): Flow<List<Theme>> {
        return themeDao.getAllThemes().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getThemeByName(name: String): Theme? {
        return themeDao.getThemeByName(name)?.toDomain()
    }
    
    override suspend fun updateTheme(theme: Theme): Result<Unit> {
        return try {
            val entity = theme.toEntity()
            themeDao.update(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateThemeName(oldName: String, newName: String): Result<Unit> {
        return try {
            themeDao.updateThemeName(oldName, newName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTheme(name: String, moveToTheme: String): Result<Unit> {
        return try {
            // 1. 将所有关联灵感移动到默认主题
            val inspirations = inspirationDao.getInspirationsByTheme(name).first()
            inspirations.forEach { inspiration ->
                val updatedInspiration = inspiration.copy(theme_name = moveToTheme)
                inspirationDao.update(updatedInspiration)
            }
            
            // 2. 删除主题
            val theme = themeDao.getThemeByName(name)
            if (theme != null) {
                themeDao.delete(theme)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Theme not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun themeExists(name: String): Boolean {
        return themeDao.themeExists(name)
    }
    
    override suspend fun updateThemeLastUsed(name: String): Result<Unit> {
        return try {
            themeDao.updateThemeLastUsed(name, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getThemesByLastUsed(): Flow<List<Theme>> {
        return themeDao.getThemesByLastUsed().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getThemesByInspirationCount(): Flow<List<Theme>> {
        return themeDao.getThemesByInspirationCount().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun refreshThemeCounts(): Result<Unit> {
        return try {
            // This would be implemented with a more complex query if needed
            // For now, we'll rely on the cached counts in the theme entity
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Extension functions for conversion
    private fun Theme.toEntity(): ThemeEntity {
        return ThemeEntity(
            name = name,
            icon = icon,
            color = color,
            description = description,
            createdAt = createdAt,
            lastUsed = lastUsed,
            inspirationCount = inspirationCount
        )
    }
    
    private fun ThemeEntity.toDomain(): Theme {
        return Theme(
            name = name,
            icon = icon,
            color = color,
            description = description,
            createdAt = createdAt,
            lastUsed = lastUsed,
            inspirationCount = inspirationCount
        )
    }
}