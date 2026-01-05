package com.sparkle.note.ui.screens.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkle.note.domain.repository.InspirationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing a theme with its statistics.
 */
data class ThemeInfo(
    val name: String,
    val inspirationCount: Int
)

/**
 * UI state for theme management screen.
 */
data class ThemeManagementUiState(
    val themes: List<ThemeInfo> = emptyList(),
    val totalInspirations: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for theme management functionality.
 * Handles theme CRUD operations and statistics.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: InspirationRepository
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
                // Get all inspirations
                val inspirations = repository.getAllInspirations().first()
                
                // Filter out theme marker inspirations
                val realInspirations = inspirations.filter { 
                    it.content != "__THEME_MARKER__"
                }
                
                // Group by theme and count (excluding theme markers)
                val themeCounts = realInspirations
                    .groupBy { it.themeName }
                    .map { (theme, inspirations) ->
                        ThemeInfo(
                            name = theme,
                            inspirationCount = inspirations.size
                        )
                    }
                    .sortedBy { it.name }
                
                _uiState.update {
                    it.copy(
                        themes = themeCounts,
                        totalInspirations = realInspirations.size,
                        isLoading = false
                    )
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
                val existingThemes = repository.getDistinctThemes().first()
                if (existingThemes.contains(themeName)) {
                    _uiState.update { it.copy(errorMessage = "主题已存在") }
                    return@launch
                }
                
                // Create a dummy inspiration with the new theme to persist it
                // This is necessary because themes are derived from existing inspirations
                // Use a special marker content to identify theme-only inspirations
                val dummyInspiration = com.sparkle.note.domain.model.Inspiration(
                    content = "__THEME_MARKER__", // Special marker for theme-only inspirations
                    themeName = themeName,
                    createdAt = System.currentTimeMillis(),
                    wordCount = 0
                )
                
                repository.saveInspiration(dummyInspiration)
                
                // Reload themes from database to ensure synchronization
                loadThemes()
                
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
                val existingThemes = repository.getDistinctThemes().first()
                if (existingThemes.contains(newName)) {
                    _uiState.update { it.copy(errorMessage = "主题名称已存在") }
                    return@launch
                }
                
                // Update all inspirations with the old theme name
                val inspirations = repository.getInspirationsByTheme(oldName).first()
                inspirations.forEach { inspiration ->
                    val updatedInspiration = inspiration.copy(themeName = newName)
                    repository.saveInspiration(updatedInspiration)
                }
                
                // Reload themes to reflect changes
                loadThemes()
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "编辑主题失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * Delete theme and move all inspirations to "未分类".
     */
    fun deleteTheme(themeName: String) {
        viewModelScope.launch {
            try {
                // Don't allow deleting the default theme
                if (themeName == "未分类") {
                    _uiState.update { it.copy(errorMessage = "不能删除默认主题") }
                    return@launch
                }
                
                // Move all inspirations to "未分类"
                val inspirations = repository.getInspirationsByTheme(themeName).first()
                inspirations.forEach { inspiration ->
                    val updatedInspiration = inspiration.copy(themeName = "未分类")
                    repository.saveInspiration(updatedInspiration)
                }
                
                // Reload themes to reflect changes
                loadThemes()
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "删除主题失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Refresh themes from database.
     * This ensures synchronization with other screens.
     */
    fun refreshThemes() {
        loadThemes()
    }
}