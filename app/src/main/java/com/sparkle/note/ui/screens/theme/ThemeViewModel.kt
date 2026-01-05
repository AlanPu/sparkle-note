package com.sparkle.note.ui.screens.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkle.note.domain.model.Theme
import com.sparkle.note.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing a theme with its statistics for UI display.
 */
data class ThemeInfo(
    val name: String,
    val icon: String,
    val color: Long,
    val description: String,
    val inspirationCount: Int,
    val createdAt: Long,
    val lastUsed: Long
)

/**
 * UI state for theme management screen.
 */
data class ThemeManagementUiState(
    val themes: List<ThemeInfo> = emptyList(),
    val totalInspirations: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val sortBy: ThemeSortBy = ThemeSortBy.NAME
)

/**
 * Theme sorting options.
 */
enum class ThemeSortBy {
    NAME, LAST_USED, INSPIRATION_COUNT
}

/**
 * ViewModel for theme management functionality.
 * Handles theme CRUD operations using independent theme entities.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ThemeManagementUiState())
    val uiState: StateFlow<ThemeManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadThemes()
    }
    
    /**
     * Load all themes with their statistics.
     */
    private fun loadThemes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Get themes based on current sort order
                val themesFlow = when (_uiState.value.sortBy) {
                    ThemeSortBy.NAME -> themeRepository.getAllThemes()
                    ThemeSortBy.LAST_USED -> themeRepository.getThemesByLastUsed()
                    ThemeSortBy.INSPIRATION_COUNT -> themeRepository.getThemesByInspirationCount()
                }
                
                themesFlow.collect { themes ->
                    val themeInfos = themes.map { theme ->
                        ThemeInfo(
                            name = theme.name,
                            icon = theme.icon,
                            color = theme.color,
                            description = theme.description,
                            inspirationCount = theme.inspirationCount,
                            createdAt = theme.createdAt,
                            lastUsed = theme.lastUsed
                        )
                    }
                    
                    val totalInspirations = themes.sumOf { it.inspirationCount }
                    
                    _uiState.update {
                        it.copy(
                            themes = themeInfos,
                            totalInspirations = totalInspirations,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "加载主题失败：${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Add a new theme.
     */
    fun addTheme(themeName: String) {
        viewModelScope.launch {
            try {
                // Validate theme name
                if (themeName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "主题名称不能为空") }
                    return@launch
                }
                
                // Check if theme already exists
                if (themeRepository.themeExists(themeName)) {
                    _uiState.update { it.copy(errorMessage = "主题已存在") }
                    return@launch
                }
                
                // Create new theme
                val newTheme = Theme(
                    name = themeName,
                    icon = "💡",
                    color = 0xFF4A90E2,
                    description = "",
                    createdAt = System.currentTimeMillis(),
                    lastUsed = System.currentTimeMillis(),
                    inspirationCount = 0
                )
                
                themeRepository.createTheme(newTheme)
                
                // Show success message and clear any error
                _uiState.update { 
                    it.copy(
                        errorMessage = null,
                        successMessage = "主题${themeName}创建成功"
                    )
                }
                
                // Clear success message after delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(successMessage = null) }
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "添加主题失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * Edit theme name.
     */
    fun editTheme(oldName: String, newName: String) {
        viewModelScope.launch {
            try {
                // Validate new name
                if (newName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "主题名称不能为空") }
                    return@launch
                }
                
                // Check if new name already exists
                if (themeRepository.themeExists(newName)) {
                    _uiState.update { it.copy(errorMessage = "主题名称已存在") }
                    return@launch
                }
                
                // Update theme name
                themeRepository.updateThemeName(oldName, newName)
                
                // Show success message
                _uiState.update { 
                    it.copy(
                        errorMessage = null,
                        successMessage = "主题已更新为${newName}"
                    )
                }
                
                // Clear success message after delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(successMessage = null) }
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "编辑主题失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * Delete theme.
     */
    fun deleteTheme(themeName: String) {
        viewModelScope.launch {
            try {
                // Don't allow deleting the default theme
                if (themeName == "未分类") {
                    _uiState.update { it.copy(errorMessage = "不能删除默认主题") }
                    return@launch
                }
                
                // Delete theme (inspirations will be moved to default theme via repository)
                themeRepository.deleteTheme(themeName)
                
                // Show success message
                _uiState.update { 
                    it.copy(
                        errorMessage = null,
                        successMessage = "主题${themeName}已删除"
                    )
                }
                
                // Clear success message after delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(successMessage = null) }
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "删除主题失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * Change theme sort order.
     */
    fun changeSortOrder(sortBy: ThemeSortBy) {
        _uiState.update { it.copy(sortBy = sortBy) }
        loadThemes()
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Refresh themes from database.
     */
    fun refreshThemes() {
        loadThemes()
    }
}