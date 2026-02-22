package top.alanpu.sparklenote.app.utils

import top.alanpu.sparklenote.app.domain.repository.InspirationRepository
import top.alanpu.sparklenote.app.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.first

/**
 * Data validation utility for verifying data integrity after import operations.
 */
object DataValidationUtil {
    
    /**
     * Validates the data integrity after import.
     * Checks for orphaned inspirations, theme consistency, and data counts.
     */
    suspend fun validateDataIntegrity(
        themeRepository: ThemeRepository,
        inspirationRepository: InspirationRepository
    ): ValidationResult {
        val themes = themeRepository.getAllThemes().first()
        val inspirations = inspirationRepository.getAllInspirations().first()
        
        val themeNames = themes.map { it.name }.toSet()
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check for orphaned inspirations (inspirations with non-existent themes)
        val orphanedInspirations = inspirations.filter { !themeNames.contains(it.themeName) }
        if (orphanedInspirations.isNotEmpty()) {
            issues.add("发现 ${orphanedInspirations.size} 个孤立灵感记录 (主题不存在)")
            orphanedInspirations.forEach { inspiration ->
                warnings.add("孤立灵感: '${inspiration.content.take(30)}...' (主题: '${inspiration.themeName}')")
            }
        }
        
        // Check theme inspiration counts
        themes.forEach { theme ->
            val themeInspirations = inspirations.filter { it.themeName == theme.name }
            val actualCount = themeInspirations.size
            
            // Note: We don't have expected count in theme, so just log for now
            if (actualCount == 0) {
                warnings.add("主题 '${theme.name}' 没有关联的灵感记录")
            }
        }
        
        // Check for duplicate themes
        val duplicateThemes = themes.groupBy { it.name }.filter { it.value.size > 1 }
        if (duplicateThemes.isNotEmpty()) {
            issues.add("发现重复主题: ${duplicateThemes.keys.joinToString(", ")}")
        }
        
        // Check for empty content inspirations
        val emptyInspirations = inspirations.filter { it.content.isBlank() }
        if (emptyInspirations.isNotEmpty()) {
            issues.add("发现 ${emptyInspirations.size} 个空内容灵感记录")
        }
        
        return ValidationResult(
            totalThemes = themes.size,
            totalInspirations = inspirations.size,
            orphanedInspirations = orphanedInspirations.size,
            issues = issues,
            warnings = warnings,
            isValid = issues.isEmpty()
        )
    }
    
    /**
     * Generates a detailed validation report.
     */
    fun generateValidationReport(result: ValidationResult): String {
        return buildString {
            appendLine("📊 数据完整性验证报告")
            appendLine("=".repeat(30))
            appendLine("主题总数: ${result.totalThemes}")
            appendLine("灵感总数: ${result.totalInspirations}")
            appendLine("孤立灵感: ${result.orphanedInspirations}")
            appendLine("状态: ${if (result.isValid) "✅ 有效" else "❌ 存在问题"}")
            
            if (result.issues.isNotEmpty()) {
                appendLine("\n🔴 发现的问题:")
                result.issues.forEach { issue ->
                    appendLine("  • $issue")
                }
            }
            
            if (result.warnings.isNotEmpty()) {
                appendLine("\n⚠️  警告信息:")
                result.warnings.take(5).forEach { warning ->
                    appendLine("  • $warning")
                }
                if (result.warnings.size > 5) {
                    appendLine("  ... 还有 ${result.warnings.size - 5} 个警告")
                }
            }
            
            if (result.isValid && result.warnings.isEmpty()) {
                appendLine("\n✅ 所有数据验证通过！")
            }
        }
    }
    
    /**
     * Result of data validation.
     */
    data class ValidationResult(
        val totalThemes: Int,
        val totalInspirations: Int,
        val orphanedInspirations: Int,
        val issues: List<String>,
        val warnings: List<String>,
        val isValid: Boolean
    )
}