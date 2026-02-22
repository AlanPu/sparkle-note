package top.alanpu.sparklenote.app.utils

import android.content.Context
import top.alanpu.sparklenote.app.domain.model.Inspiration
import top.alanpu.sparklenote.app.domain.repository.InspirationRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced backup manager with JSON serialization and comprehensive backup functionality.
 * Supports automatic backup management and export/import operations.
 */
@Singleton
class EnhancedBackupManager @Inject constructor(
    private val inspirationRepository: InspirationRepository,
    private val context: Context
) {
    
    companion object {
        private const val BACKUP_DIR = "backups"
        private const val MAX_BACKUPS = 10
        private const val DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"
    }
    
    /**
     * Creates a backup with the given name.
     */
    suspend fun createBackup(name: String): Result<String> {
        return try {
            val inspirations = inspirationRepository.getAllInspirations().first()
            val themes = inspirationRepository.getDistinctThemes().first()
            
            val backupJson = BackupManager.createBackup(inspirations, themes)
            
            // Create backup directory if it doesn't exist
            val backupDir = File(getBackupDirectory())
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            // Generate filename
            val backupDate = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val fileName = "${name}_$backupDate.json"
            val backupFile = File(backupDir, fileName)
            
            // Write backup file
            backupFile.writeText(backupJson)
            
            // Manage backup count (keep only the most recent ones)
            manageBackupCount()
            
            Result.success(fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Creates a backup with specific content.
     */
    suspend fun createBackup(filename: String, content: String): Result<Unit> {
        return try {
            // Create backup directory if it doesn't exist
            val backupDir = File(getBackupDirectory())
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val backupFile = File(backupDir, filename)
            backupFile.writeText(content)
            
            // Manage backup count
            manageBackupCount()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Restores data from a backup file.
     */
    suspend fun restoreBackup(backupFileName: String): Result<Unit> {
        return try {
            val backupDir = File(getBackupDirectory())
            val backupFile = File(backupDir, backupFileName)
            
            if (!backupFile.exists()) {
                return Result.failure(Exception("Backup file not found"))
            }
            
            val backupData = BackupManager.parseBackup(backupFile.readText())
                ?: return Result.failure(Exception("Invalid backup format"))
            
            // Clear existing data
            val existingInspirations = inspirationRepository.getAllInspirations().first()
            existingInspirations.forEach { inspiration ->
                inspirationRepository.deleteInspiration(inspiration.id)
            }
            
            // Restore inspirations from backup
            backupData.inspirations.forEach { inspirationBackup ->
                val inspiration = Inspiration(
                    id = 0, // Let Room generate new ID
                    content = inspirationBackup.content,
                    themeName = inspirationBackup.themeName,
                    createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(inspirationBackup.createdAt)?.time ?: System.currentTimeMillis(),
                    wordCount = inspirationBackup.wordCount
                )
                inspirationRepository.saveInspiration(inspiration)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reads backup file content.
     */
    fun readBackupFile(backupFileName: String): String {
        val backupDir = File(getBackupDirectory())
        val backupFile = File(backupDir, backupFileName)
        return if (backupFile.exists()) {
            backupFile.readText()
        } else {
            ""
        }
    }
    
    /**
     * Gets a list of all available backups.
     */
    fun getAllBackups(): List<String> {
        val backupDir = File(getBackupDirectory())
        if (!backupDir.exists()) {
            return emptyList()
        }
        
        return backupDir.listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { it.name }
            ?: emptyList()
    }
    
    /**
     * Gets a specific backup file.
     */
    fun getBackupFile(backupFileName: String): File {
        val backupDir = File(getBackupDirectory())
        return File(backupDir, backupFileName)
    }
    
    /**
     * Gets the URI for a backup file.
     */
    fun getBackupFileUri(backupFile: File): android.net.Uri {
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
    }
    
    /**
     * Gets the latest backup file.
     */
    fun getLatestBackup(): File? {
        val backupDir = File(getBackupDirectory())
        if (!backupDir.exists()) return null
        
        return backupDir.listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
            ?.firstOrNull()
    }
    
    /**
     * Calculates total size of all backups.
     */
    fun getTotalBackupSize(): Long {
        val backupDir = File(getBackupDirectory())
        if (!backupDir.exists()) return 0
        
        return backupDir.listFiles { file -> file.extension == "json" }
            ?.sumOf { it.length() } ?: 0
    }
    
    /**
     * Deletes a backup.
     */
    fun deleteBackup(backupFileName: String): Result<Unit> {
        return try {
            val backupDir = File(getBackupDirectory())
            val backupFile = File(backupDir, backupFileName)
            if (backupFile.exists()) {
                backupFile.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Shares a backup file.
     */
    fun shareBackup(context: Context, backupFileName: String): Result<Unit> {
        return try {
            val backupDir = File(getBackupDirectory())
            val backupFile = File(backupDir, backupFileName)
            if (!backupFile.exists()) {
                return Result.failure(Exception("Backup file not found"))
            }
            
            // Create share intent
            val uri = getBackupFileUri(backupFile)
            
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Sparkle Note Backup: $backupFileName")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = android.content.Intent.createChooser(shareIntent, "Share Backup")
            context.startActivity(chooser)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Manages the number of backups to keep only the most recent ones.
     */
    private fun manageBackupCount() {
        val backupDir = File(getBackupDirectory())
        if (!backupDir.exists()) return
        
        val backupFiles = backupDir.listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() } ?: return
        
        if (backupFiles.size > MAX_BACKUPS) {
            // Delete the oldest backups
            backupFiles.drop(MAX_BACKUPS).forEach { file ->
                file.delete()
            }
        }
    }
    
    /**
     * Gets the backup directory path.
     */
    private fun getBackupDirectory(): String {
        return File(context.filesDir, BACKUP_DIR).absolutePath
    }
    
    /**
     * Gets the application context.
     */
    fun getContext(): Context {
        return context
    }
}