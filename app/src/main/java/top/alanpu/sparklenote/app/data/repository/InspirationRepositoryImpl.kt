package top.alanpu.sparklenote.app.data.repository

import top.alanpu.sparklenote.app.data.entity.InspirationEntity
import top.alanpu.sparklenote.app.domain.model.Inspiration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for inspiration data operations.
 * Provides data access layer with Room database integration.
 */
@Singleton
class InspirationRepositoryImpl @Inject constructor(
    private val inspirationDao: top.alanpu.sparklenote.app.data.database.dao.InspirationDao,
    private val themeDao: top.alanpu.sparklenote.app.data.database.dao.ThemeDao
) : top.alanpu.sparklenote.app.domain.repository.InspirationRepository {
    
    override suspend fun saveInspiration(inspiration: Inspiration): Result<Unit> {
        return try {
            println("💾 开始保存灵感: ${inspiration.content.take(30)}...")
            val entity = inspiration.toEntity()
            val insertedId = inspirationDao.insert(entity)
            println("✅ 灵感保存成功，ID: $insertedId")
            
            // 更新主题的最后使用时间和统计信息
            println("🔄 更新主题统计: ${inspiration.themeName}")
            updateThemeStats(inspiration.themeName)
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ 灵感保存失败: ${e.message}")
            Result.failure(e)
        }
    }
    
    override fun getAllInspirations(): Flow<List<Inspiration>> {
        return inspirationDao.getAllInspirations().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getDistinctThemes(): Flow<List<String>> {
        return inspirationDao.getDistinctThemes()
    }
    
    override fun searchInspirations(keyword: String): Flow<List<Inspiration>> {
        return inspirationDao.searchInspirations(keyword).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getInspirationsByTheme(themeName: String): Flow<List<Inspiration>> {
        return inspirationDao.getInspirationsByTheme(themeName).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun deleteInspiration(id: Long): Result<Unit> {
        return try {
            // 获取被删除的灵感以更新主题统计
            val inspiration = inspirationDao.getInspirationById(id)
            inspiration?.let {
                inspirationDao.deleteById(id)
                // 更新主题统计信息
                updateThemeStats(it.theme_name)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteInspirationsByTheme(themeName: String): Result<Unit> {
        return try {
            inspirationDao.deleteByThemeName(themeName)
            // 更新主题统计信息
            updateThemeStats(themeName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun exportToMarkdown(inspirations: List<Inspiration>): String {
        return top.alanpu.sparklenote.app.utils.ExportManager.exportBatchToMarkdown(inspirations)
    }
    
    override fun exportSingleToMarkdown(inspiration: Inspiration): String {
        return top.alanpu.sparklenote.app.utils.ExportManager.exportSingleToMarkdown(inspiration)
    }
    
    /**
     * 更新主题的统计信息，包括最后使用时间和灵感数量
     */
    private suspend fun updateThemeStats(themeName: String) {
        try {
            // 更新最后使用时间
            themeDao.updateThemeLastUsed(themeName, System.currentTimeMillis())
            
            // 更新灵感数量
            val count = inspirationDao.getInspirationCountByTheme(themeName)
            themeDao.updateThemeInspirationCount(themeName, count.toInt())
        } catch (e: Exception) {
            // 记录错误但不影响主要功能
            e.printStackTrace()
        }
    }
}

/**
 * Extension function to convert Inspiration domain model to InspirationEntity.
 */
private fun Inspiration.toEntity(): InspirationEntity {
    return InspirationEntity(
        id = this.id,
        content = this.content,
        theme_name = this.themeName,
        created_at = this.createdAt,
        word_count = this.wordCount
    )
}

/**
 * Extension function to convert InspirationEntity to Inspiration domain model.
 */
private fun InspirationEntity.toDomain(): Inspiration {
    return Inspiration(
        id = this.id,
        content = this.content,
        themeName = this.theme_name,
        createdAt = this.created_at,
        wordCount = this.word_count
    )
}