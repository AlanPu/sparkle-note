package com.sparkle.note.ui.screens.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkle.note.domain.repository.InspirationRepository
import com.sparkle.note.domain.repository.ThemeRepository
import com.sparkle.note.utils.BackupCompatibilityProcessor
import com.sparkle.note.utils.BackupFileImporter
import com.sparkle.note.utils.BackupManager
import com.sparkle.note.utils.BackupPreview
import com.sparkle.note.utils.DataValidationUtil
import com.sparkle.note.utils.EnhancedBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for backup management screen.
 */
data class BackupManagementUiState(
    val backups: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastBackupDate: java.util.Date? = null,
    val totalSize: Long = 0,
    val externalBackupPreview: BackupPreview? = null,
    val lastImportMessage: String? = null,
    val lastImportSuccess: Boolean? = null
)

/**
 * ViewModel for backup management functionality.
 * Handles backup creation, restoration, and external file import.
 */
@HiltViewModel
class BackupManagementViewModel @Inject constructor(
    private val enhancedBackupManager: EnhancedBackupManager,
    private val inspirationRepository: InspirationRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {
    
    private val backupFileImporter = BackupFileImporter(enhancedBackupManager.getContext())
    
    private val _uiState = MutableStateFlow(BackupManagementUiState())
    val uiState: StateFlow<BackupManagementUiState> = _uiState.asStateFlow()
    
    private var currentExternalBackupData: com.sparkle.note.utils.BackupData? = null
    
    init {
        loadBackups()
    }
    
    /**
     * Loads all available backups.
     */
    fun loadBackups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val backups = enhancedBackupManager.getAllBackups()
                val totalSize = enhancedBackupManager.getTotalBackupSize()
                val lastBackup = enhancedBackupManager.getLatestBackup()
                
                _uiState.update { currentState ->
                    currentState.copy(
                        backups = backups,
                        totalSize = totalSize,
                        lastBackupDate = lastBackup?.let { 
                            java.util.Date(it.lastModified()) 
                        },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "加载备份失败: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Creates a new backup with the given name.
     */
    fun createBackup(backupName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val inspirations = inspirationRepository.getAllInspirations().first()
                val themes = themeRepository.getAllThemes().first()
                val themeNames = themes.map { it.name }
                
                val backupContent = BackupManager.createBackup(inspirations, themeNames)
                val filename = "${backupName}.json"
                
                enhancedBackupManager.createBackup(filename, backupContent)
                
                // Reload backups
                loadBackups()
                
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "创建备份失败: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Validates and previews external backup file.
     */
    fun validateExternalBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                when (val result = backupFileImporter.validateAndParseBackup(uri)) {
                    is BackupFileImporter.ValidationResult.Success -> {
                        currentExternalBackupData = result.backupData
                        val preview = backupFileImporter.generateBackupPreview(result.backupData)
                        
                        _uiState.update { currentState ->
                            currentState.copy(
                                externalBackupPreview = preview,
                                isLoading = false
                            )
                        }
                    }
                    is BackupFileImporter.ValidationResult.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                lastImportMessage = "验证失败: ${result.message}",
                                lastImportSuccess = false,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        lastImportMessage = "文件验证失败: ${e.message}",
                        lastImportSuccess = false,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Imports external backup data.
     */
    fun importExternalBackup() {
        viewModelScope.launch {
            val backupData = currentExternalBackupData
            if (backupData == null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        lastImportMessage = "没有可用的备份数据",
                        lastImportSuccess = false
                    )
                }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                var successCount = 0
                var errorCount = 0
                val themeImportResults = mutableListOf<String>()
                val inspirationImportResults = mutableListOf<String>()
                
                // 首先处理备份数据兼容性，确保所有主题都存在
                println("🔍 开始处理备份数据兼容性...")
                val compatibilityResult = BackupCompatibilityProcessor.processBackupData(backupData, themeRepository)
                
                if (!compatibilityResult.canProceed) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            lastImportMessage = "兼容性处理失败: ${compatibilityResult.message}",
                            lastImportSuccess = false,
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                val processedBackupData = compatibilityResult.processedBackupData
                if (compatibilityResult.missingThemes.isNotEmpty()) {
                    themeImportResults.add("ℹ️ 自动创建缺失主题: ${compatibilityResult.missingThemes.size} 个")
                    println("ℹ️ 自动创建缺失主题: ${compatibilityResult.missingThemes.size} 个")
                }
                
                // Import themes first - collect all theme names for verification
                val importedThemes = mutableSetOf<String>()
                val existingThemes = mutableSetOf<String>()
                
                processedBackupData.themes?.forEach { themeBackup ->
                    try {
                        val theme = com.sparkle.note.domain.model.Theme(
                            name = themeBackup.name,
                            icon = themeBackup.icon,
                            color = java.lang.Long.parseLong(themeBackup.color, 16),
                            description = "", // Default description
                            createdAt = System.currentTimeMillis(),
                            lastUsed = System.currentTimeMillis()
                        )
                        
                        if (!themeRepository.themeExists(theme.name)) {
                            val result = themeRepository.createTheme(theme)
                            if (result.isSuccess) {
                                successCount++
                                importedThemes.add(theme.name)
                                themeImportResults.add("✓ 主题: ${theme.name}")
                                println("✅ 主题创建成功: ${theme.name}")
                            } else {
                            errorCount++
                            themeImportResults.add("✗ 主题: ${theme.name} - ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        existingThemes.add(theme.name)
                        themeImportResults.add("~ 主题已存在: ${theme.name}")
                    }
                } catch (e: Exception) {
                    errorCount++
                    themeImportResults.add("✗ 主题错误: ${themeBackup.name} - ${e.message}")
                }
            }
            
            // Verify all themes are available before importing inspirations
            val allAvailableThemes = importedThemes + existingThemes
                
                // Import inspirations with proper theme verification and mapping
                processedBackupData.inspirations?.forEach { inspirationBackup ->
                    try {
                        // Apply theme mapping if exists
                        val actualThemeName = compatibilityResult.themeMappings[inspirationBackup.themeName] ?: inspirationBackup.themeName
                        
                        // Verify theme exists in our collected list
                        if (!allAvailableThemes.contains(actualThemeName)) {
                            errorCount++
                            inspirationImportResults.add("✗ 灵感: 主题 '${inspirationBackup.themeName}' -> '$actualThemeName' 不可用")
                            return@forEach
                        }
                        
                        // Show mapping information if different
                        val mappingInfo = if (actualThemeName != inspirationBackup.themeName) {
                            " (${inspirationBackup.themeName} -> $actualThemeName)"
                        } else {
                            ""
                        }
                        
                        val inspiration = com.sparkle.note.domain.model.Inspiration(
                            content = inspirationBackup.content,
                            themeName = actualThemeName,
                            createdAt = System.currentTimeMillis(), // Use current time for import
                            wordCount = inspirationBackup.wordCount
                        )
                        
                        val result = inspirationRepository.saveInspiration(inspiration)
                        if (result.isSuccess) {
                            successCount++
                            inspirationImportResults.add("✓ 灵感: ${inspiration.content.take(20)}...$mappingInfo")
                        } else {
                            errorCount++
                            inspirationImportResults.add("✗ 灵感: ${result.exceptionOrNull()?.message}$mappingInfo")
                        }
                    } catch (e: Exception) {
                        errorCount++
                        inspirationImportResults.add("✗ 灵感错误: ${e.message}")
                        println("❌ 灵感错误: ${e.message}")
                    }
                }
                
                // Clear external backup data
                currentExternalBackupData = null
                
                val detailedMessage = buildString {
                    appendLine("📊 导入统计")
                    appendLine("✅ 成功: $successCount")
                    appendLine("❌ 失败: $errorCount")
                    appendLine("📋 主题: ${importedThemes.size} 新, ${existingThemes.size} 已存在")
                    
                    // 显示兼容性处理信息
                    if (compatibilityResult.missingThemes.isNotEmpty()) {
                        appendLine("ℹ️ 自动创建主题: ${compatibilityResult.missingThemes.size} 个")
                    }
                    if (compatibilityResult.themeMappings.size > compatibilityResult.missingThemes.size) {
                        appendLine("🎯 智能匹配主题: ${compatibilityResult.themeMappings.size - compatibilityResult.missingThemes.size} 个")
                    }
                    
                    if (themeImportResults.isNotEmpty()) {
                        appendLine("\n🎯 主题详情:")
                        themeImportResults.take(3).forEach { appendLine(it) }
                        if (themeImportResults.size > 3) appendLine("... 还有 ${themeImportResults.size - 3} 个")
                    }
                    
                    if (inspirationImportResults.isNotEmpty()) {
                        appendLine("\n💡 灵感详情:")
                        inspirationImportResults.take(3).forEach { appendLine(it) }
                        if (inspirationImportResults.size > 3) appendLine("... 还有 ${inspirationImportResults.size - 3} 个")
                    }
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        externalBackupPreview = null,
                        lastImportMessage = detailedMessage,
                        lastImportSuccess = errorCount == 0,
                        isLoading = false
                    )
                }
                
                // Force refresh data after import
                loadBackups()
                
                // Perform comprehensive data validation
                viewModelScope.launch {
                    try {
                        println("🔍 开始导入后数据验证...")
                        
                        // Basic counts
                        val themeCount = themeRepository.getAllThemes().first().size
                        val inspirationCount = inspirationRepository.getAllInspirations().first().size
                        println("📊 导入验证 - 主题总数: $themeCount, 灵感总数: $inspirationCount")
                        
                        // Data integrity validation
                        val validationResult = DataValidationUtil.validateDataIntegrity(themeRepository, inspirationRepository)
                        val validationReport = DataValidationUtil.generateValidationReport(validationResult)
                        println(validationReport)
                        
                        // Update UI with validation results
                        val finalMessage = buildString {
                            appendLine(_uiState.value.lastImportMessage ?: "")
                            appendLine("\n📊 数据验证 - 主题: $themeCount, 灵感: $inspirationCount")
                            if (!validationResult.isValid || validationResult.warnings.isNotEmpty()) {
                                appendLine("\n" + validationReport)
                            }
                        }
                        
                        _uiState.update { currentState ->
                            currentState.copy(
                                lastImportMessage = finalMessage,
                                lastImportSuccess = validationResult.isValid && errorCount == 0
                            )
                        }
                        
                        println("✅ 导入验证完成")
                    } catch (e: Exception) {
                        println("❌ 导入验证失败: ${e.message}")
                    }
                }
                
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        lastImportMessage = "导入失败: ${e.message}",
                        lastImportSuccess = false,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Clears external backup data.
     */
    fun clearExternalBackup() {
        currentExternalBackupData = null
        _uiState.update { it.copy(externalBackupPreview = null) }
    }
    
    /**
     * Restores data from a backup file.
     */
    fun restoreBackup(backupFileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val backupContent = enhancedBackupManager.readBackupFile(backupFileName)
                val backupData = BackupManager.parseBackup(backupContent)
                
                if (backupData == null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            errorMessage = "备份文件格式不正确",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Clear existing data and restore from backup
                val inspirations = inspirationRepository.getAllInspirations().first()
                val themes = themeRepository.getAllThemes().first()
                println("🗑️ 清理现有数据 - 灵感数量: ${inspirations.size}, 主题数量: ${themes.size}")
                
                inspirations.forEach { inspiration ->
                    inspirationRepository.deleteInspiration(inspiration.id)
                }
                println("✅ 清理完成")
                
                // Restore themes and inspirations
                var restoredThemes = 0
                var restoredInspirations = 0
                
                backupData.themes?.forEach { themeBackup ->
                    try {
                        val theme = com.sparkle.note.domain.model.Theme(
                            name = themeBackup.name,
                            icon = themeBackup.icon,
                            color = java.lang.Long.parseLong(themeBackup.color, 16),
                            description = "",
                            createdAt = System.currentTimeMillis(),
                            lastUsed = System.currentTimeMillis()
                        )
                        
                        if (!themeRepository.themeExists(theme.name)) {
                            val result = themeRepository.createTheme(theme)
                            if (result.isSuccess) {
                                restoredThemes++
                                println("✅ 主题创建成功: ${theme.name}")
                            } else {
                                println("❌ 主题创建失败: ${theme.name} - ${result.exceptionOrNull()?.message}")
                            }
                        } else {
                            println("~ 主题已存在: ${theme.name}")
                        }
                    } catch (e: Exception) {
                        println("❌ 主题恢复错误: ${themeBackup.name} - ${e.message}")
                    }
                }
                
                backupData.inspirations?.forEach { inspirationBackup ->
                    try {
                        // Verify theme exists
                        val themeExists = themeRepository.themeExists(inspirationBackup.themeName)
                        if (!themeExists) {
                            println("❌ 灵感导入失败 - 主题不存在: '${inspirationBackup.themeName}'")
                            return@forEach
                        }
                        
                        val inspiration = com.sparkle.note.domain.model.Inspiration(
                            content = inspirationBackup.content,
                            themeName = inspirationBackup.themeName,
                            createdAt = System.currentTimeMillis(),
                            wordCount = inspirationBackup.wordCount
                        )
                        
                        val result = inspirationRepository.saveInspiration(inspiration)
                        if (result.isSuccess) {
                            restoredInspirations++
                            println("✅ 灵感创建成功: ${inspiration.content.take(30)}...")
                        } else {
                            println("❌ 灵感创建失败: ${result.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        println("❌ 灵感恢复错误: ${e.message}")
                    }
                }
                
                println("📊 恢复完成 - 主题: $restoredThemes, 灵感: $restoredInspirations")
                
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "数据恢复成功！主题: $restoredThemes, 灵感: $restoredInspirations",
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "恢复失败: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Deletes a backup file.
     */
    fun deleteBackup(backupFileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                enhancedBackupManager.deleteBackup(backupFileName)
                loadBackups() // Reload backups
                
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "删除备份失败: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Shares a backup file.
     */
    fun shareBackup(context: Context, backupFileName: String) {
        viewModelScope.launch {
            try {
                val backupFile = enhancedBackupManager.getBackupFile(backupFileName)
                if (backupFile.exists()) {
                    val uri = enhancedBackupManager.getBackupFileUri(backupFile)
                    
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Sparkle Note 备份文件")
                        putExtra(Intent.EXTRA_TEXT, "这是 Sparkle Note 的备份文件，包含您的所有灵感和主题数据。")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    val chooser = Intent.createChooser(shareIntent, "分享备份文件")
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "分享失败: ${e.message}"
                    )
                }
            }
        }
    }
}