package com.sparkle.note.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkle.note.domain.model.Inspiration
import com.sparkle.note.domain.repository.InspirationRepository
import com.sparkle.note.domain.repository.ThemeRepository
import com.sparkle.note.utils.SearchHistoryManager
import com.sparkle.note.utils.SearchSuggestionManager
import com.sparkle.note.ui.screens.main.TimeFilter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * UI state for advanced search screen.
 */
data class AdvancedSearchUiState(
    val searchQuery: String = "",
    val selectedTheme: String? = null,
    val selectedThemes: List<String> = emptyList(), // Multi-theme support
    val isMultiThemeMode: Boolean = false, // Toggle for multi-theme filtering
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val searchResults: List<Inspiration> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),
    val availableThemes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for advanced search functionality.
 * Manages complex search with multiple criteria and suggestions.
 */
@HiltViewModel
class AdvancedSearchViewModel @Inject constructor(
    private val repository: InspirationRepository,
    private val themeRepository: ThemeRepository,
    private val searchHistoryManager: SearchHistoryManager
) : ViewModel() {
    
    private val searchSuggestionManager = SearchSuggestionManager(repository)
    
    private val _uiState = MutableStateFlow(AdvancedSearchUiState())
    val uiState: StateFlow<AdvancedSearchUiState> = _uiState.asStateFlow()
    
    init {
        loadAvailableThemes()
        loadSearchHistory()
    }
    
    /**
     * Updates the search query and generates suggestions.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        viewModelScope.launch {
            if (query.isNotBlank()) {
                searchSuggestionManager.getSearchSuggestions(query)
                    .collect { suggestions ->
                        _uiState.update { currentState ->
                            currentState.copy(searchSuggestions = suggestions)
                        }
                    }
            } else {
                _uiState.update { it.copy(searchSuggestions = emptyList()) }
            }
        }
    }
    
    /**
     * Updates the selected theme filter.
     */
    fun updateSelectedTheme(theme: String?) {
        _uiState.update { it.copy(selectedTheme = theme) }
    }
    
    /**
     * Toggles multi-theme mode.
     */
    fun toggleMultiThemeMode() {
        _uiState.update { currentState ->
            val newMode = !currentState.isMultiThemeMode
            currentState.copy(
                isMultiThemeMode = newMode,
                selectedTheme = if (newMode) null else currentState.selectedTheme,
                selectedThemes = if (newMode) currentState.selectedThemes else emptyList()
            )
        }
    }
    
    /**
     * Toggles a theme in multi-theme selection.
     */
    fun toggleThemeSelection(theme: String) {
        _uiState.update { currentState ->
            val currentThemes = currentState.selectedThemes.toMutableList()
            if (currentThemes.contains(theme)) {
                currentThemes.remove(theme)
            } else {
                currentThemes.add(theme)
            }
            currentState.copy(selectedThemes = currentThemes)
        }
    }
    
    /**
     * Clears all selected themes.
     */
    fun clearAllSelectedThemes() {
        _uiState.update { it.copy(selectedThemes = emptyList()) }
    }
    
    /**
     * Updates the time filter.
     */
    fun updateTimeFilter(timeFilter: TimeFilter) {
        _uiState.update { it.copy(timeFilter = timeFilter) }
    }
    
    /**
     * Performs the search with current criteria.
     */
    fun performSearch() {
        val currentState = _uiState.value
        val hasThemeFilter = if (currentState.isMultiThemeMode) {
            currentState.selectedThemes.isNotEmpty()
        } else {
            currentState.selectedTheme != null
        }
        
        if (currentState.searchQuery.isBlank() && !hasThemeFilter) {
            _uiState.update { it.copy(errorMessage = "请输入搜索关键词或选择筛选条件") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Add to search history
                if (currentState.searchQuery.isNotBlank()) {
                    searchHistoryManager.addSearchQuery(currentState.searchQuery)
                }
                
                // Get all inspirations
                val allInspirations = repository.getAllInspirations().first()
                
                // Apply filters
                var resultInspirations = allInspirations
                
                // Apply text search
                if (currentState.searchQuery.isNotBlank()) {
                    val query = currentState.searchQuery.lowercase()
                    resultInspirations = resultInspirations.filter { inspiration ->
                        inspiration.content.lowercase().contains(query) ||
                        inspiration.themeName.lowercase().contains(query)
                    }
                }
                
                // Apply theme filter
                if (currentState.isMultiThemeMode && currentState.selectedThemes.isNotEmpty()) {
                    // Multi-theme filter
                    resultInspirations = resultInspirations.filter { inspiration ->
                        currentState.selectedThemes.contains(inspiration.themeName)
                    }
                } else if (currentState.selectedTheme != null) {
                    // Single theme filter
                    resultInspirations = resultInspirations.filter { 
                        it.themeName == currentState.selectedTheme 
                    }
                }
                
                // Apply time filter
                val now = System.currentTimeMillis()
                resultInspirations = when (currentState.timeFilter) {
                    TimeFilter.TODAY -> resultInspirations.filter { isToday(it.createdAt, now) }
                    TimeFilter.THIS_WEEK -> resultInspirations.filter { isThisWeek(it.createdAt, now) }
                    TimeFilter.THIS_MONTH -> resultInspirations.filter { isThisMonth(it.createdAt, now) }
                    TimeFilter.ALL -> resultInspirations
                }
                
                _uiState.update { 
                    it.copy(
                        searchResults = resultInspirations,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "搜索失败: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Clears the current search.
     */
    fun clearSearch() {
        _uiState.update { 
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                searchSuggestions = emptyList()
            )
        }
    }
    
    /**
     * Clears search history.
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryManager.clearSearchHistory()
            _uiState.update { it.copy(searchHistory = emptyList()) }
        }
    }
    
    /**
     * Loads available themes.
     */
    private fun loadAvailableThemes() {
        viewModelScope.launch {
            themeRepository.getAllThemes()
                .collect { themes ->
                    val themeNames = themes.map { it.name }
                    _uiState.update { it.copy(availableThemes = themeNames) }
                }
        }
    }
    
    /**
     * Loads search history.
     */
    private fun loadSearchHistory() {
        viewModelScope.launch {
            searchHistoryManager.searchHistory
                .collect { history ->
                    _uiState.update { it.copy(searchHistory = history) }
                }
        }
    }
    
    /**
     * Helper function to check if timestamp is today.
     */
    private fun isToday(timestamp: Long, now: Long): Boolean {
        val dayInMillis = 24 * 60 * 60 * 1000L
        return now - timestamp < dayInMillis
    }
    
    /**
     * Helper function to check if timestamp is this week.
     */
    private fun isThisWeek(timestamp: Long, now: Long): Boolean {
        val weekInMillis = 7 * 24 * 60 * 60 * 1000L
        return now - timestamp < weekInMillis
    }
    
    /**
     * Helper function to check if timestamp is this month.
     */
    private fun isThisMonth(timestamp: Long, now: Long): Boolean {
        val monthInMillis = 30L * 24 * 60 * 60 * 1000L
        return now - timestamp < monthInMillis
    }
}