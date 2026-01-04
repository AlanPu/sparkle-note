package com.sparkle.note.utils

import com.sparkle.note.domain.model.Inspiration
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages backup and restore operations for inspiration data.
 * Handles JSON serialization and file operations.
 */
object BackupManager {
    
    private val jsonFormat = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Creates a backup of inspiration data in JSON format.
     * 
     * @param inspirations List of inspirations to backup
     * @param themes List of theme names
     * @return JSON string containing the backup data
     */
    fun createBackup(
        inspirations: List<Inspiration>,
        themes: List<String>
    ): String {
        val backupData = BackupData(
            version = "1.0",
            exportTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
            appVersion = "1.0.0",
            totalInspirations = inspirations.size,
            totalThemes = themes.size,
            themes = themes.map { themeName ->
                ThemeBackup(
                    name = themeName,
                    icon = getThemeEmoji(themeName),
                    color = "#FF4A90E2", // Default Nordic blue
                    inspirationCount = inspirations.count { it.themeName == themeName }
                )
            },
            inspirations = inspirations.map { inspiration ->
                InspirationBackup(
                    id = inspiration.id,
                    content = inspiration.content,
                    themeName = inspiration.themeName,
                    createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date(inspiration.createdAt)),
                    wordCount = inspiration.wordCount
                )
            }
        )
        
        return jsonFormat.encodeToString(backupData)
    }
    
    /**
     * Parses backup JSON and extracts inspiration data.
     * 
     * @param jsonString JSON string to parse
     * @return Parsed backup data or null if parsing fails
     */
    fun parseBackup(jsonString: String): BackupData? {
        return try {
            jsonFormat.decodeFromString<BackupData>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Generates backup filename with timestamp.
     * 
     * @return Filename string in format "sparkle-backup-YYYYMMDD-HHmmss.json"
     */
    fun generateBackupFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        return "sparkle-backup-$timestamp.json"
    }
    
    /**
     * Returns appropriate emoji for theme.
     */
    private fun getThemeEmoji(themeName: String): String {
        return when (themeName.lowercase()) {
            "产品设计" -> "💡"
            "技术开发" -> "⚙️"
            "生活感悟" -> "🌟"
            "工作思考" -> "💼"
            "学习笔记" -> "📚"
            "创意想法" -> "🎨"
            else -> "💭"
        }
    }
}

/**
 * Data class for complete backup structure.
 */
@kotlinx.serialization.Serializable
data class BackupData(
    val version: String,
    val exportTime: String,
    val appVersion: String,
    val totalInspirations: Int,
    val totalThemes: Int,
    val themes: List<ThemeBackup>,
    val inspirations: List<InspirationBackup>
)

/**
 * Data class for theme backup information.
 */
@kotlinx.serialization.Serializable
data class ThemeBackup(
    val name: String,
    val icon: String,
    val color: String,
    val inspirationCount: Int
)

/**
 * Data class for inspiration backup information.
 */
@kotlinx.serialization.Serializable
data class InspirationBackup(
    val id: Long,
    val content: String,
    val themeName: String,
    val createdAt: String,
    val wordCount: Int
)