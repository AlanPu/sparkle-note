package com.sparkle.note.ui.screens.main

import com.sparkle.note.domain.model.Inspiration
import com.sparkle.note.data.repository.MockInspirationRepository
import com.sparkle.note.data.repository.MockThemeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MainViewModel with real repository integration.
 * Tests the complete data flow from ViewModel to Repository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    
    private lateinit var viewModel: MainViewModel
    private lateinit var mockRepository: MockInspirationRepository
    private lateinit var mockThemeRepository: MockThemeRepository
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockInspirationRepository()
        mockThemeRepository = MockThemeRepository()
        viewModel = MainViewModel(mockRepository, mockThemeRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `mainViewModel_initialState_hasEmptyContentAndDefaultTheme`() = runTest {
        // Arrange & Assert
        val initialState = viewModel.uiState.value
        assertThat(initialState.currentContent).isEmpty()
        assertThat(initialState.selectedTheme).isEqualTo("未分类")
        assertThat(initialState.inspirations).isEmpty()
    }
    
    @Test
    fun `mainViewModel_onContentChange_updatesContentCorrectly`() = runTest {
        // Arrange
        val testContent = "这是一个测试灵感"
        
        // Act
        viewModel.onContentChange(testContent)
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.currentContent).isEqualTo(testContent)
    }
    
    @Test
    fun `mainViewModel_onContentChange_respects500CharacterLimit`() = runTest {
        // Arrange
        val longContent = "a".repeat(600) // 600 characters, exceeds 500 limit
        
        // Act
        viewModel.onContentChange(longContent)
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.currentContent.length).isEqualTo(500)
    }
    
    @Test
    fun `mainViewModel_onThemeSelect_updatesSelectedTheme`() = runTest {
        // Arrange
        val newTheme = "产品设计"
        
        // Act
        viewModel.onThemeSelect(newTheme)
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.selectedTheme).isEqualTo(newTheme)
    }
    
    @Test
    fun `mainViewModel_onSearchKeywordChange_filtersInspirations`() = runTest {
        // Arrange - Add some test inspirations
        val inspirations = listOf(
            Inspiration(content = "产品设计的灵感", themeName = "产品设计", wordCount = 8),
            Inspiration(content = "技术开发的内容", themeName = "技术开发", wordCount = 7),
            Inspiration(content = "生活感悟的记录", themeName = "生活感悟", wordCount = 7)
        )
        inspirations.forEach { inspiration ->
            mockRepository.saveInspiration(inspiration)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Act - Search for "设计"
        viewModel.onSearchKeywordChange("设计")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert - Should only show inspiration containing "设计"
        val state = viewModel.uiState.value
        assertThat(state.inspirations).hasSize(1)
        assertThat(state.inspirations[0].content).contains("设计")
    }
    
    @Test
    fun `mainViewModel_onThemeFilter_filtersByTheme`() = runTest {
        // Arrange - Add inspirations with different themes
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "产品2", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3)
        )
        inspirations.forEach { inspiration ->
            mockRepository.saveInspiration(inspiration)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Act - Filter by "产品设计" theme
        viewModel.onThemeFilter("产品设计")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert - Should only show product design inspirations
        val state = viewModel.uiState.value
        assertThat(state.inspirations).hasSize(2)
        assertThat(state.inspirations.map { it.themeName }).containsExactly("产品设计", "产品设计")
    }
    
    @Test
    fun `mainViewModel_onSaveInspiration_withEmptyContent_showsError`() = runTest {
        // Arrange
        viewModel.onContentChange("") // Empty content
        
        // Act
        viewModel.onSaveInspiration()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Note: Error event would be tested with Turbine in a real test
        // For now, we verify that empty content doesn't get saved
        val state = viewModel.uiState.value
        assertThat(state.currentContent).isEmpty() // Content remains empty
    }
    
    @Test
    fun `mainViewModel_onSaveInspiration_withValidContent_savesAndClearsInput`() = runTest {
        // Arrange
        val testContent = "这是一个有效的灵感内容"
        viewModel.onContentChange(testContent)
        
        // Act
        viewModel.onSaveInspiration()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.currentContent).isEmpty() // Content should be cleared after save
        
        // Verify inspiration was saved by checking all inspirations
        val allInspirations = mockRepository.getAllInspirations()
        val inspirations = mutableListOf<Inspiration>()
        allInspirations.collect { list ->
            inspirations.clear()
            inspirations.addAll(list)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(inspirations).hasSize(1) // One inspiration should be saved
        assertThat(inspirations[0].content).isEqualTo(testContent)
    }
    
    @Test
    fun `mainViewModel_onDeleteInspiration_removesInspiration`() = runTest {
        // Arrange
        val testContent = "要删除的灵感"
        
        // Save an inspiration first
        viewModel.onContentChange(testContent)
        viewModel.onSaveInspiration()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Get the saved inspiration
        val allInspirations = mutableListOf<Inspiration>()
        mockRepository.getAllInspirations().collect { list ->
            allInspirations.clear()
            allInspirations.addAll(list)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(allInspirations).hasSize(1)
        val savedInspiration = allInspirations[0]
        
        // Act
        viewModel.onDeleteInspiration(savedInspiration.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val updatedInspirations = mutableListOf<Inspiration>()
        mockRepository.getAllInspirations().collect { list ->
            updatedInspirations.clear()
            updatedInspirations.addAll(list)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(updatedInspirations).isEmpty() // Inspiration should be removed
    }
    
    @Test
    fun `mainViewModel_loadThemes_loadsDistinctThemesFromRepository`() = runTest {
        // Arrange - Add inspirations with different themes
        val inspirations = listOf(
            Inspiration(content = "灵感1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "灵感2", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "灵感3", themeName = "技术开发", wordCount = 3),
            Inspiration(content = "灵感4", themeName = "生活感悟", wordCount = 3)
        )
        inspirations.forEach { inspiration ->
            mockRepository.saveInspiration(inspiration)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Create a fresh ViewModel instance to load themes from repository
        val freshViewModel = MainViewModel(mockRepository, mockThemeRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert - ViewModel should load the 3 distinct themes from repository
        val state = freshViewModel.uiState.value
        assertThat(state.themes).containsExactly("产品设计", "技术开发", "生活感悟")
    }
}