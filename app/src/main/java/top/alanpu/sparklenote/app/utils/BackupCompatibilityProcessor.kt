package top.alanpu.sparklenote.app.utils

import top.alanpu.sparklenote.app.utils.BackupData
import top.alanpu.sparklenote.app.utils.InspirationBackup
import top.alanpu.sparklenote.app.utils.ThemeBackup
import top.alanpu.sparklenote.app.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.first
import java.text.Normalizer
import kotlin.math.abs

/**
 * Backup data compatibility processor for handling old backup formats.
 * Provides theme mapping and auto-creation for missing themes.
 */
object BackupCompatibilityProcessor {
    
    /**
     * Processes backup data to ensure compatibility with current theme structure.
     * 
     * @param backupData Original backup data
     * @param themeRepository Theme repository for checking existing themes
     * @return Processed backup data with guaranteed theme availability
     */
    suspend fun processBackupData(
        backupData: BackupData,
        themeRepository: ThemeRepository
    ): CompatibilityResult {
        println("🔍 开始处理备份数据兼容性...")
        
        val existingThemes = themeRepository.getAllThemes().first()
        val existingThemeNames = existingThemes.map { it.name }.toSet()
        val backupThemeNames = backupData.themes?.map { it.name }?.toSet() ?: emptySet()
        val inspirationThemeNames = backupData.inspirations?.map { it.themeName }?.toSet() ?: emptySet()
        
        println("📊 现有主题: ${existingThemeNames.size} 个")
        println("📊 备份主题: ${backupThemeNames.size} 个") 
        println("📊 灵感主题: ${inspirationThemeNames.size} 个")
        
        // 智能主题匹配和映射
        val themeMappings = mutableMapOf<String, String>() // 原始主题名 -> 匹配的主题名
        val trulyMissingThemes = mutableSetOf<String>()
        
        // 处理所有需要的主题
        val allRequiredThemes = backupThemeNames + inspirationThemeNames
        allRequiredThemes.forEach { requiredTheme ->
            if (existingThemeNames.contains(requiredTheme)) {
                // 主题已存在，直接映射
                themeMappings[requiredTheme] = requiredTheme
            } else {
                // 尝试智能匹配
                val bestMatch = ThemeNameMatcher.findBestMatch(requiredTheme, existingThemeNames.toList())
                if (bestMatch != null) {
                    println("🎯 智能匹配: '$requiredTheme' -> '$bestMatch'")
                    themeMappings[requiredTheme] = bestMatch
                } else {
                    // 确实缺失的主题
                    trulyMissingThemes.add(requiredTheme)
                }
            }
        }
        
        println("🎯 主题映射: ${themeMappings.size} 个")
        println("🔍 缺失主题: ${trulyMissingThemes.size} 个")
        
        // 为缺失的主题提供建议
        trulyMissingThemes.forEach { missingTheme ->
            val suggestions = ThemeNameMatcher.suggestAlternatives(missingTheme, existingThemeNames.toList())
            if (suggestions.isNotEmpty()) {
                println("💡 主题 '$missingTheme' 的建议替代: ${suggestions.joinToString(", ")}")
            }
        }
        
        println("🔍 缺失主题: ${trulyMissingThemes.size} 个")
        
        if (trulyMissingThemes.isEmpty() && themeMappings.size == existingThemeNames.size) {
            println("✅ 所有主题都已存在或已智能匹配，无需处理")
            return CompatibilityResult(
                processedBackupData = backupData,
                missingThemes = emptyList(),
                themeMappings = themeMappings,
                canProceed = true,
                message = "所有主题都已存在或已智能匹配"
            )
        }
        
        // 为确实缺失的主题创建默认主题数据
        val missingThemeBackups = trulyMissingThemes.map { themeName ->
            createDefaultThemeBackup(themeName)
        }
        
        println("🎯 为 ${trulyMissingThemes.size} 个缺失主题创建默认数据")
        
        // 构建处理后的备份数据
        val processedThemes = (backupData.themes ?: emptyList()) + missingThemeBackups
        val processedBackupData = backupData.copy(
            themes = processedThemes,
            totalThemes = processedThemes.size
        )
        
        println("✅ 备份数据兼容性处理完成")
        
        return CompatibilityResult(
            processedBackupData = processedBackupData,
            missingThemes = missingThemeBackups,
            themeMappings = themeMappings,
            canProceed = true,
            message = "已处理 ${trulyMissingThemes.size} 个缺失主题，智能匹配 ${themeMappings.size - trulyMissingThemes.size} 个主题"
        )
    }
    
    /**
     * Creates a default theme backup for missing themes.
     */
    private fun createDefaultThemeBackup(themeName: String): ThemeBackup {
        val defaultIcon = getDefaultIconForTheme(themeName)
        val defaultColor = getDefaultColorForTheme(themeName)
        
        return ThemeBackup(
            name = themeName,
            icon = defaultIcon,
            color = defaultColor,
            inspirationCount = 0 // Will be updated during import
        )
    }
    
    /**
     * Gets default icon based on theme name.
     */
    private fun getDefaultIconForTheme(themeName: String): String {
        return when (themeName.lowercase()) {
            "工作", "work" -> "💼"
            "学习", "study", "education" -> "📚"
            "生活", "life", "daily" -> "🌟"
            "创意", "creative", "idea" -> "💡"
            "技术", "tech", "technology" -> "⚙️"
            "健康", "health", "fitness" -> "💪"
            "旅行", "travel", "trip" -> "✈️"
            "美食", "food", "cooking" -> "🍳"
            "运动", "sport", "exercise" -> "🏃"
            "音乐", "music", "song" -> "🎵"
            "电影", "movie", "film" -> "🎬"
            "读书", "book", "reading" -> "📖"
            "游戏", "game", "gaming" -> "🎮"
            else -> "💭" // 默认图标
        }
    }
    
    /**
     * Gets default color based on theme name.
     */
    private fun getDefaultColorForTheme(themeName: String): String {
        return when (themeName.lowercase()) {
            "工作", "work" -> "FF2196F3" // 蓝色
            "学习", "study", "education" -> "FF4CAF50" // 绿色
            "生活", "life", "daily" -> "FFFF9800" // 橙色
            "创意", "creative", "idea" -> "FF9C27B0" // 紫色
            "技术", "tech", "technology" -> "FF607D8B" // 蓝灰色
            "健康", "health", "fitness" -> "FF4CAF50" // 绿色
            "旅行", "travel", "trip" -> "FF03A9F4" // 天蓝色
            "美食", "food", "cooking" -> "FFFF5722" // 深橙色
            "运动", "sport", "exercise" -> "FFFF5252" // 红色
            "音乐", "music", "song" -> "FF9C27B0" // 紫色
            "电影", "movie", "film" -> "FF795548" // 棕色
            "读书", "book", "reading" -> "FF8BC34A" // 浅绿色
            "游戏", "game", "gaming" -> "FF673AB7" // 深紫色
            else -> "FF9E9E9E" // 默认灰色
        }
    }
    
    /**
     * Result of backup compatibility processing.
     */
    data class CompatibilityResult(
        val processedBackupData: BackupData,
        val missingThemes: List<ThemeBackup>,
        val themeMappings: Map<String, String>,
        val canProceed: Boolean,
        val message: String
    )
}