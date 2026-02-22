package top.alanpu.sparklenote.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

/**
 * Manages file selection and import operations for backup files.
 * Handles external file access and backup data validation.
 */
class BackupFileImporter(
    private val context: Context
) {
    
    private val jsonFormat = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Creates an intent for selecting JSON backup files.
     */
    fun createFileSelectionIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain"))
            putExtra(Intent.EXTRA_TITLE, "选择备份文件")
        }
    }
    
    /**
     * Validates and parses backup data from a URI.
     * 
     * @param uri The URI of the selected file
     * @return ValidationResult containing the parsed backup data or error information
     */
    suspend fun validateAndParseBackup(uri: Uri): ValidationResult {
        return try {
            val backupContent = readFileContent(uri)
            if (backupContent.isNullOrBlank()) {
                return ValidationResult.Error("文件内容为空")
            }
            
            val backupData = parseBackupContent(backupContent)
            if (backupData == null) {
                return ValidationResult.Error("备份文件格式不正确")
            }
            
            // Validate backup structure
            val validationErrors = validateBackupStructure(backupData)
            if (validationErrors.isNotEmpty()) {
                return ValidationResult.Error("备份文件验证失败: ${validationErrors.joinToString(", ")}")
            }
            
            ValidationResult.Success(backupData)
            
        } catch (e: SecurityException) {
            ValidationResult.Error("无法访问文件，请检查文件权限")
        } catch (e: Exception) {
            ValidationResult.Error("文件读取失败: ${e.message}")
        }
    }
    
    /**
     * Reads file content from URI.
     */
    private suspend fun readFileContent(uri: Uri): String? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                reader.readText()
            }
        }
    }
    
    /**
     * Parses backup content from JSON string.
     */
    private fun parseBackupContent(content: String): BackupData? {
        return try {
            jsonFormat.decodeFromString<BackupData>(content)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Validates the structure of backup data.
     */
    private fun validateBackupStructure(backupData: BackupData): List<String> {
        val errors = mutableListOf<String>()
        
        println("🔍 开始验证备份数据结构")
        println("📋 备份版本: ${backupData.version}")
        println("📋 主题数量: ${backupData.themes?.size ?: 0}")
        println("📋 灵感数量: ${backupData.inspirations?.size ?: 0}")
        
        // Check version compatibility
        if (backupData.version.isNullOrBlank()) {
            errors.add("缺少版本信息")
            println("❌ 缺少版本信息")
        } else if (!isVersionCompatible(backupData.version)) {
            errors.add("不兼容的备份版本: ${backupData.version}")
            println("❌ 版本不兼容: ${backupData.version}")
        } else {
            println("✅ 版本兼容: ${backupData.version}")
        }
        
        // Check themes
        if (backupData.themes.isNullOrEmpty()) {
            errors.add("缺少主题数据")
            println("⚠️ 主题数据为空")
        } else {
            println("✅ 找到 ${backupData.themes.size} 个主题")
            backupData.themes.forEachIndexed { index, theme ->
                if (theme.name.isNullOrBlank()) {
                    errors.add("主题${index + 1}名称为空")
                    println("❌ 主题${index + 1}名称为空")
                } else {
                    println("✅ 主题${index + 1}: ${theme.name}")
                }
            }
        }
        
        // Check inspirations
        if (backupData.inspirations.isNullOrEmpty()) {
            errors.add("缺少灵感数据")
            println("⚠️ 灵感数据为空")
        } else {
            println("✅ 找到 ${backupData.inspirations.size} 个灵感")
            backupData.inspirations.forEachIndexed { index, inspiration ->
                when {
                    inspiration.content.isNullOrBlank() -> {
                        errors.add("灵感${index + 1}内容为空")
                        println("❌ 灵感${index + 1}内容为空")
                    }
                    inspiration.content.length > 1000 -> {
                        errors.add("灵感${index + 1}内容过长")
                        println("❌ 灵感${index + 1}内容过长 (${inspiration.content.length} 字符)")
                    }
                    inspiration.themeName.isNullOrBlank() -> {
                        errors.add("灵感${index + 1}缺少主题")
                        println("❌ 灵感${index + 1}缺少主题")
                    }
                    else -> {
                        println("✅ 灵感${index + 1}: ${inspiration.content.take(30)}... (主题: ${inspiration.themeName})")
                    }
                }
            }
        }
        
        println("🔍 验证完成，发现 ${errors.size} 个错误")
        return errors
    }
    
    /**
     * Checks if the backup version is compatible with current app.
     */
    private fun isVersionCompatible(version: String): Boolean {
        return when (version) {
            "1.0" -> true
            else -> false
        }
    }
    
    /**
     * Generates a preview summary of the backup data.
     */
    fun generateBackupPreview(backupData: BackupData): BackupPreview {
        return BackupPreview(
            totalInspirations = backupData.inspirations?.size ?: 0,
            totalThemes = backupData.themes?.size ?: 0,
            exportTime = backupData.exportTime ?: "未知",
            appVersion = backupData.appVersion ?: "未知",
            themeDistribution = backupData.themes?.groupBy { it.name }
                ?.mapValues { it.value.size } ?: emptyMap()
        )
    }
    
    /**
     * Sealed class for validation results.
     */
    sealed class ValidationResult {
        data class Success(val backupData: BackupData) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}

/**
 * Preview information for backup data.
 */
data class BackupPreview(
    val totalInspirations: Int,
    val totalThemes: Int,
    val exportTime: String,
    val appVersion: String,
    val themeDistribution: Map<String, Int>
)