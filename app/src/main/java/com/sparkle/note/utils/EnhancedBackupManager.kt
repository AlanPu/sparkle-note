package com.sparkle.note.utils

import android.content.Context
import com.sparkle.note.domain.model.Inspiration
import com.sparkle.note.domain.repository.InspirationRepository
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
    private val inspirationRepository: InspirationRepository
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
     * Gets a list of all available backups.
     */
    fun getBackupList(): List<String> {
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
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
            
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Sparkle Note Backup: $backupFileName")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = android.content.Intent.createChooser(shareIntent, "分享备份")
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
        return "/data/data/com.sparkle.note/files/$BACKUP_DIR"
    }
}