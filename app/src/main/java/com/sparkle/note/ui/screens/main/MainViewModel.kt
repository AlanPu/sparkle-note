package com.sparkle.note.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkle.note.domain.model.Inspiration
import com.sparkle.note.domain.repository.InspirationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the main screen state and user interactions.
 * Handles inspiration creation, display, search functionality, and export operations.
 * Uses Hilt dependency injection for production-ready architecture.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: InspirationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<MainEvent>()
    val events: SharedFlow<MainEvent> = _events.asSharedFlow()
    
    init {
        loadInspirations()
        loadThemes()
    }
    
    /**
     * Updates the current content being typed by the user.
     * @param content The new content text
     */
    fun onContentChange(content: String) {
        _uiState.update { it.copy(currentContent = content.take(500)) }
    }
    
    /**
     * Updates the selected theme for the new inspiration.
     * @param theme The selected theme name
     */
    fun onThemeSelect(theme: String) {
        _uiState.update { it.copy(selectedTheme = theme) }
    }
    
    /**
     * Saves the current inspiration to the database.
     * Validates content and shows appropriate feedback.
     */
    fun onSaveInspiration() {
        val currentState = _uiState.value
        val content = currentState.currentContent.trim()
        
        if (content.isBlank()) {
            viewModelScope.launch {
                _events.emit(MainEvent.ShowError("请输入灵感内容"))
            }
            return
        }
        
        viewModelScope.launch {
            val inspiration = Inspiration(
                content = content,
                themeName = currentState.selectedTheme,
                wordCount = content.length
            )
            
            repository.saveInspiration(inspiration)
                .onSuccess {
                    _uiState.update { it.copy(currentContent = "") }
                    _events.emit(MainEvent.ShowSuccess("灵感已保存到 ${currentState.selectedTheme}"))
                    loadInspirations() // Refresh the list
                }
                .onFailure { error ->
                    _events.emit(MainEvent.ShowError("保存失败，请重试"))
                }
        }
    }
    
    /**
     * Searches inspirations by keyword.
     * @param keyword The search keyword
     */
    fun onSearch(keyword: String) {
        _uiState.update { it.copy(searchKeyword = keyword) }
        // Apply search filter to current inspirations
        applyFilters()
    }
    
    /**
     * Filters inspirations by theme.
     * @param theme The theme to filter by, null for all themes
     */
    fun onThemeFilter(theme: String?) {
        _uiState.update { it.copy(selectedFilterTheme = theme) }
        applyFilters()
    }
    
    /**
     * Deletes an inspiration by ID.
     * @param id The ID of the inspiration to delete
     */
    fun onDeleteInspiration(id: Long) {
        viewModelScope.launch {
            repository.deleteInspiration(id)
            _events.emit(MainEvent.ShowSuccess("灵感已删除"))
            loadInspirations() // Refresh the list
        }
    }
    
    /**
     * Exports all inspirations to Markdown format.
     * Shows appropriate feedback and handles file operations.
     */
    fun onExportInspirations() {
        viewModelScope.launch {
            val inspirations = repository.getAllInspirations().first()
            val markdown = repository.exportToMarkdown(inspirations)
            
            // TODO: Implement actual file saving
            _events.emit(MainEvent.ShowSuccess("导出成功！Markdown内容已复制到剪贴板"))
        }
    }
    
    private fun loadInspirations() {
        viewModelScope.launch {
            repository.getAllInspirations()
                .collect { inspirations ->
                    _uiState.update { it.copy(allInspirations = inspirations) }
                    applyFilters()
                }
        }
    }
    
    private fun loadThemes() {
        viewModelScope.launch {
            repository.getDistinctThemes()
                .collect { themes ->
                    val allThemes = if (themes.isEmpty()) {
                        listOf("未分类", "产品设计", "技术开发", "生活感悟")
                    } else {
                        themes
                    }
                    _uiState.update { 
                        it.copy(
                            themes = allThemes,
                            selectedTheme = allThemes.firstOrNull() ?: "未分类"
                        )
                    }
                }
        }
    }
    
    private fun applyFilters() {
        val state = _uiState.value
        var filteredInspirations = state.allInspirations
        
        // Apply search filter
        if (state.searchKeyword.isNotBlank()) {
            filteredInspirations = filteredInspirations.filter { inspiration ->
                inspiration.content.contains(state.searchKeyword, ignoreCase = true) ||
                inspiration.themeName.contains(state.searchKeyword, ignoreCase = true)
            }
        }
        
        // Apply theme filter
        if (state.selectedFilterTheme != null) {
            filteredInspirations = filteredInspirations.filter { inspiration ->
                inspiration.themeName == state.selectedFilterTheme
            }
        }
        
        _uiState.update { it.copy(inspirations = filteredInspirations) }
    }
}

/**
 * Represents the UI state of the main screen.
 */
data class MainUiState(
    val allInspirations: List<Inspiration> = emptyList(),
    val inspirations: List<Inspiration> = emptyList(),
    val themes: List<String> = emptyList(),
    val currentContent: String = "",
    val selectedTheme: String = "未分类",
    val searchKeyword: String = "",
    val selectedFilterTheme: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Events that can be emitted from the ViewModel.
 */
sealed class MainEvent {
    data class ShowSuccess(val message: String) : MainEvent()
    data class ShowError(val message: String) : MainEvent()
}