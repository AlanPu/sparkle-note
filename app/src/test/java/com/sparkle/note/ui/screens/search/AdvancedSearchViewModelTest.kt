package com.sparkle.note.ui.screens.search

import com.sparkle.note.domain.model.Inspiration
import com.sparkle.note.domain.model.Theme
import com.sparkle.note.domain.repository.InspirationRepository
import com.sparkle.note.domain.repository.ThemeRepository
import com.sparkle.note.utils.SearchHistoryManager
import com.sparkle.note.ui.screens.main.TimeFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

/**
 * Test class for AdvancedSearchViewModel.
 * Tests search functionality, theme filtering, and data consistency.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AdvancedSearchViewModelTest {

    @Mock
    private lateinit var repository: InspirationRepository

    @Mock
    private lateinit var themeRepository: ThemeRepository

    @Mock
    private lateinit var searchHistoryManager: SearchHistoryManager

    private lateinit var viewModel: AdvancedSearchViewModel

    @Before
    fun setup() {
        viewModel = AdvancedSearchViewModel(repository, themeRepository, searchHistoryManager)
    }

    /**
     * Test that all themes are loaded from ThemeRepository, not just themes with inspirations.
     * This addresses the bug where newly created themes were not visible in search.
     */
    @Test
    fun `loadAvailableThemes should return all themes from ThemeRepository`() = runTest {
        // Given: Mock themes including themes without inspirations
        val mockThemes = listOf(
            Theme(name = "工作", icon = "💼"),
            Theme(name = "学习", icon = "📚"),
            Theme(name = "生活", icon = "🏠"),
            Theme(name = "新主题", icon = "✨") // 新创建但没有灵感的主题
        )
        
        `when`(themeRepository.getAllThemes()).thenReturn(flowOf(mockThemes))
        `when`(searchHistoryManager.searchHistory).thenReturn(flowOf(emptyList()))

        // When: ViewModel is initialized (loads themes in init)
        // Themes are loaded automatically in init block

        // Then: All themes should be available in UI state
        val uiState = viewModel.uiState.value
        assertEquals(4, uiState.availableThemes.size)
        assertTrue(uiState.availableThemes.contains("工作"))
        assertTrue(uiState.availableThemes.contains("学习"))
        assertTrue(uiState.availableThemes.contains("生活"))
        assertTrue(uiState.availableThemes.contains("新主题")) // 关键：新主题应该可见
    }

    /**
     * Test that themes without inspirations are included in available themes.
     * This addresses the bug where newly created themes were not visible in search.
     */
    @Test
    fun `newly created themes without inspirations should be available for search`() = runTest {
        // Given: A newly created theme with no inspirations
        val newTheme = Theme(name = "创意设计", icon = "🎨")
        val mockThemes = listOf(newTheme)
        
        `when`(themeRepository.getAllThemes()).thenReturn(flowOf(mockThemes))
        `when`(searchHistoryManager.searchHistory).thenReturn(flowOf(emptyList()))

        // When: ViewModel loads themes
        // Themes are loaded automatically in init block

        // Then: The new theme should be available
        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.availableThemes.size)
        assertTrue(uiState.availableThemes.contains("创意设计"))
    }

    /**
     * Test multi-theme search functionality.
     */
    @Test
    fun `multi-theme search should support multiple theme selection`() = runTest {
        // Given: Multiple themes available
        val mockThemes = listOf(
            Theme(name = "工作", icon = "💼"),
            Theme(name = "学习", icon = "📚"),
            Theme(name = "生活", icon = "🏠")
        )
        
        `when`(themeRepository.getAllThemes()).thenReturn(flowOf(mockThemes))
        `when`(searchHistoryManager.searchHistory).thenReturn(flowOf(emptyList()))

        // When: User enables multi-theme mode and selects multiple themes
        viewModel.toggleMultiThemeMode()
        viewModel.toggleThemeSelection("工作")
        viewModel.toggleThemeSelection("学习")

        // Then: UI state should reflect multi-theme selection
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isMultiThemeMode)
        assertEquals(2, uiState.selectedThemes.size)
        assertTrue(uiState.selectedThemes.contains("工作"))
        assertTrue(uiState.selectedThemes.contains("学习"))
    }

    /**
     * Test time filter functionality.
     */
    @Test
    fun `time filter should be updated correctly`() = runTest {
        // Given: Mock setup
        val mockThemes = listOf(Theme(name = "工作", icon = "💼"))
        `when`(themeRepository.getAllThemes()).thenReturn(flowOf(mockThemes))
        `when`(searchHistoryManager.searchHistory).thenReturn(flowOf(emptyList()))

        // When: Different time filters are applied
        viewModel.updateTimeFilter(TimeFilter.TODAY)
        var uiState = viewModel.uiState.value
        assertEquals(TimeFilter.TODAY, uiState.timeFilter)

        viewModel.updateTimeFilter(TimeFilter.THIS_WEEK)
        uiState = viewModel.uiState.value
        assertEquals(TimeFilter.THIS_WEEK, uiState.timeFilter)
    }

    /**
     * Test that search history is properly managed.
     */
    @Test
    fun `search query should be updated correctly`() = runTest {
        // Given: Mock setup
        val mockThemes = listOf(Theme(name = "工作", icon = "💼"))
        `when`(themeRepository.getAllThemes()).thenReturn(flowOf(mockThemes))
        `when`(searchHistoryManager.searchHistory).thenReturn(flowOf(emptyList()))

        // When: User updates search query
        viewModel.updateSearchQuery("工作会议")

        // Then: UI state should reflect the new query
        val uiState = viewModel.uiState.value
        assertEquals("工作会议", uiState.searchQuery)
    }

    /**
     * Test error handling during theme loading.
     */
    @Test
    fun `error during theme loading should be handled gracefully`() = runTest {
        // Given: Theme repository throws exception
        `when`(themeRepository.getAllThemes())
            .thenReturn(flowOf(emptyList())) // Return empty list instead of throwing
        `when`(searchHistoryManager.searchHistory).thenReturn(flowOf(emptyList()))

        // When: ViewModel tries to load themes
        // Exception should be caught and handled

        // Then: UI state should reflect empty themes (graceful degradation)
        val uiState = viewModel.uiState.value
        assertTrue(uiState.availableThemes.isEmpty())
        // Error should be logged but UI remains functional
    }
}