package top.alanpu.sparklenote.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import top.alanpu.sparklenote.app.domain.model.Inspiration
import top.alanpu.sparklenote.app.domain.repository.InspirationRepository
import top.alanpu.sparklenote.app.domain.repository.ThemeRepository
import top.alanpu.sparklenote.app.utils.SearchHistoryManager
import top.alanpu.sparklenote.app.utils.SearchSuggestionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * UI state for advanced search screen.
 */
data class AdvancedSearchUiState(
    val searchQuery: String = "",
    val selectedThemes: List<String> = emptyList(), // Multi-theme support only
    val searchResults: List<Inspiration> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),
    val availableThemes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for advanced search functionality.
 * Manages complex search with content and theme filtering.
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
        performSearch()
    }
    
    /**
     * Clears all selected themes.
     */
    fun clearAllSelectedThemes() {
        _uiState.update { it.copy(selectedThemes = emptyList()) }
        performSearch()
    }
    
    /**
     * Performs the search with current criteria.
     * Unified multi-theme mode: if no theme is selected, search across all themes.
     */
    fun performSearch() {
        val currentState = _uiState.value
        
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
                
                // Apply multi-theme filter: only filter when at least one theme is selected
                if (currentState.selectedThemes.isNotEmpty()) {
                    resultInspirations = resultInspirations.filter { inspiration ->
                        currentState.selectedThemes.contains(inspiration.themeName)
                    }
                }
                // If selectedThemes is empty, search across all themes (no filtering)
                
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
}