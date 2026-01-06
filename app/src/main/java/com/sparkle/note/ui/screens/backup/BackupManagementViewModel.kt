package com.sparkle.note.ui.screens.backup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkle.note.domain.repository.InspirationRepository
import com.sparkle.note.utils.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for backup management screen.
 * Handles backup creation, restoration, and management operations.
 */
@HiltViewModel
class BackupManagementViewModel @Inject constructor(
    private val backupManager: BackupManager,
    private val inspirationRepository: InspirationRepository,
    private val application: android.app.Application
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BackupManagementUiState())
    val uiState: StateFlow<BackupManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadBackupList()
    }
    
    private fun loadBackupList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Get backup files from app's backup directory
                val backupDir = getBackupDirectory()
                val backupFiles = if (backupDir.exists()) {
                    backupDir.listFiles { file -> file.name.endsWith(".json") }
                        ?.sortedByDescending { it.lastModified() }
                        ?: emptyList()
                } else {
                    emptyList()
                }
                
                // Get the most recent backup date
                val lastBackupDate = backupFiles.firstOrNull()?.lastModified()?.let { Date(it) }
                
                // Get backup file names
                val backupFileNames = backupFiles.map { it.name }
                
                val totalSize = backupFiles.sumOf { file ->
                    file.length()
                }
                
                _uiState.update { 
                    it.copy(
                        backups = backupFileNames,
                        totalSize = totalSize,
                        lastBackupDate = lastBackupDate,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun createBackup(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Get current data
                val inspirations = inspirationRepository.getAllInspirations().first()
                val themes = inspirationRepository.getDistinctThemes().first()
                
                // Create backup JSON
                val backupJson = backupManager.createBackup(inspirations, themes)
                
                // Save to file
                val backupDir = getBackupDirectory()
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                
                val fileName = if (name.isNotBlank()) {
                    "${name.replace(" ", "_")}.json"
                } else {
                    backupManager.generateBackupFilename()
                }
                
                val backupFile = File(backupDir, fileName)
                backupFile.writeText(backupJson)
                
                // Reload list
                loadBackupList()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun restoreBackup(backupFileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val backupDir = getBackupDirectory()
                val backupFile = File(backupDir, backupFileName)
                
                if (!backupFile.exists()) {
                    _uiState.update { 
                        it.copy(
                            error = "备份文件不存在",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                val backupJson = backupFile.readText()
                val backupData = backupManager.parseBackup(backupJson)
                
                if (backupData == null) {
                    _uiState.update { 
                        it.copy(
                            error = "备份文件格式错误",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Clear existing data
                inspirationRepository.getAllInspirations().first().forEach { inspiration ->
                    inspirationRepository.deleteInspiration(inspiration.id)
                }
                
                // Restore inspirations
                backupData.inspirations.forEach { inspirationBackup ->
                    val inspiration = com.sparkle.note.domain.model.Inspiration(
                        id = 0, // Let database generate new ID
                        content = inspirationBackup.content,
                        themeName = inspirationBackup.themeName,
                        createdAt = System.currentTimeMillis(), // Use current time
                        wordCount = inspirationBackup.wordCount
                    )
                    inspirationRepository.saveInspiration(inspiration)
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        lastRestoreMessage = "备份已成功恢复"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun deleteBackup(backupFileName: String) {
        viewModelScope.launch {
            try {
                val backupDir = getBackupDirectory()
                val backupFile = File(backupDir, backupFileName)
                
                if (backupFile.exists()) {
                    backupFile.delete()
                    loadBackupList() // Reload the list
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun shareBackup(context: Context, backupFileName: String) {
        viewModelScope.launch {
            try {
                val backupDir = getBackupDirectory()
                val backupFile = File(backupDir, backupFileName)
                
                if (backupFile.exists()) {
                    // Create share intent
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        backupFile
                    )
                    
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Sparkle Note 备份")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "分享备份"))
                } else {
                    _uiState.update { 
                        it.copy(error = "备份文件不存在")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearLastRestoreMessage() {
        _uiState.update { it.copy(lastRestoreMessage = null) }
    }
    
    private fun getBackupDirectory(): File {
        return File(application.getExternalFilesDir(null), "backups")
    }
}

/**
 * UI state for backup management screen
 */
data class BackupManagementUiState(
    val backups: List<String> = emptyList(), // List of backup filenames
    val totalSize: Long = 0,
    val lastBackupDate: Date? = null, // Date of the most recent backup
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastRestoreMessage: String? = null
)