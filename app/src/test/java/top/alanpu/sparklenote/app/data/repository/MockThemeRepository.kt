package top.alanpu.sparklenote.app.data.repository

import top.alanpu.sparklenote.app.domain.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * Mock implementation of ThemeRepository for testing purposes.
 * Provides in-memory theme storage with realistic behavior.
 */
class MockThemeRepository : top.alanpu.sparklenote.app.domain.repository.ThemeRepository {
    
    private val themes = ConcurrentHashMap<String, Theme>()
    
    init {
        // 初始化默认主题
        val defaultThemes = listOf("未分类", "产品设计", "技术开发", "生活感悟")
        defaultThemes.forEach { themeName ->
            themes[themeName] = Theme(
                name = themeName,
                icon = "💡",
                color = 0xFF4A90E2,
                description = "",
                createdAt = System.currentTimeMillis(),
                lastUsed = System.currentTimeMillis(),
                inspirationCount = 0
            )
        }
    }
    
    override suspend fun createTheme(theme: Theme): Result<Unit> {
        return try {
            // 检查主题是否已存在
            if (themes.containsKey(theme.name)) {
                return Result.failure(Exception("Theme already exists"))
            }
            themes[theme.name] = theme
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllThemes(): Flow<List<Theme>> {
        return flow {
            emit(themes.values.toList().sortedBy { it.name })
        }
    }
    
    override suspend fun getThemeByName(name: String): Theme? {
        return themes[name]
    }
    
    override suspend fun updateTheme(theme: Theme): Result<Unit> {
        return try {
            if (themes.containsKey(theme.name)) {
                themes[theme.name] = theme
                Result.success(Unit)
            } else {
                Result.failure(Exception("Theme not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateThemeName(oldName: String, newName: String): Result<Unit> {
        return try {
            // 检查新名称是否已存在
            if (themes.containsKey(newName)) {
                return Result.failure(Exception("Theme name already exists"))
            }
            
            val theme = themes[oldName]
            if (theme != null) {
                themes.remove(oldName)
                themes[newName] = theme.copy(name = newName)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Theme not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTheme(name: String, moveToTheme: String): Result<Unit> {
        return try {
            if (themes.containsKey(name)) {
                themes.remove(name)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Theme not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun themeExists(name: String): Boolean {
        return themes.containsKey(name)
    }
    
    override suspend fun updateThemeLastUsed(name: String): Result<Unit> {
        return try {
            val theme = themes[name]
            if (theme != null) {
                themes[name] = theme.copy(lastUsed = System.currentTimeMillis())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Theme not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getThemesByLastUsed(): Flow<List<Theme>> {
        return flow {
            emit(themes.values.toList().sortedByDescending { it.lastUsed })
        }
    }
    
    override fun getThemesByInspirationCount(): Flow<List<Theme>> {
        return flow {
            emit(themes.values.toList().sortedByDescending { it.inspirationCount })
        }
    }
    
    override suspend fun refreshThemeCounts(): Result<Unit> {
        // 在mock实现中，统计信息已经是实时的
        return Result.success(Unit)
    }
}