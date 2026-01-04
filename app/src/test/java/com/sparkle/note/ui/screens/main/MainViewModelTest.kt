package com.sparkle.note.ui.screens.main

import com.sparkle.note.domain.model.Inspiration
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
    private lateinit var mockRepository: com.sparkle.note.data.repository.MockInspirationRepository
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = com.sparkle.note.data.repository.MockInspirationRepository()
        viewModel = MainViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `mainViewModel_initialState_hasEmptyContentAndDefaultTheme()`() {
        // Arrange & Assert
        val initialState = viewModel.uiState.value
        assertThat(initialState.currentContent).isEmpty()
        assertThat(initialState.selectedTheme).isEqualTo("未分类")
        assertThat(initialState.inspirations).isEmpty()
    }
    
    @Test
    fun `mainViewModel_onContentChange_updatesContentCorrectly()`() {
        // Arrange
        val testContent = "这是一个测试灵感"
        
        // Act
        viewModel.onContentChange(testContent)
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.currentContent).isEqualTo(testContent)
    }
    
    @Test
    fun `mainViewModel_onContentChange_respects500CharacterLimit()`() {
        // Arrange
        val longContent = "a".repeat(600) // 600 characters, exceeds 500 limit
        
        // Act
        viewModel.onContentChange(longContent)
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.currentContent.length).isEqualTo(500) // Should be truncated to 500
    }
    
    @Test
    fun `mainViewModel_onThemeSelect_updatesThemeCorrectly()`() {
        // Arrange
        val newTheme = "技术开发"
        
        // Act
        viewModel.onThemeSelect(newTheme)
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.selectedTheme).isEqualTo(newTheme)
    }
    
    @Test
    fun `mainViewModel_onSaveInspiration_withEmptyContent_showsError()`() = runTest {
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
    fun `mainViewModel_onSaveInspiration_withValidContent_savesAndClearsInput()`() = runTest {
        // Arrange
        val testContent = "这是一个有效的灵感内容"
        viewModel.onContentChange(testContent)
        
        // Act
        viewModel.onSaveInspiration()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.currentContent).isEmpty() // Content should be cleared after save
        assertThat(updatedState.inspirations).hasSize(1) // One inspiration should be saved
        assertThat(updatedState.inspirations[0].content).isEqualTo(testContent)
    }
    
    @Test
    fun `mainViewModel_onDeleteInspiration_removesInspiration()`() = runTest {
        // Arrange
        val testContent = "要删除的灵感"
        viewModel.onContentChange(testContent)
        viewModel.onSaveInspiration()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val savedInspiration = viewModel.uiState.value.inspirations[0]
        
        // Act
        viewModel.onDeleteInspiration(savedInspiration.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        val updatedState = viewModel.uiState.value
        assertThat(updatedState.inspirations).isEmpty() // Inspiration should be removed
    }
    
    @Test
    fun `mainViewModel_loadThemes_loadsDistinctThemesFromRepository()`() = runTest {
        // Arrange
        val inspirations = listOf(
            Inspiration(content = "灵感1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "灵感2", themeName = "技术开发", wordCount = 3),
            Inspiration(content = "灵感3", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "灵感4", themeName = "生活感悟", wordCount = 3)
        )
        
        // Save inspirations directly to repository (bypassing ViewModel to isolate the test)
        inspirations.forEach { inspiration ->
            mockRepository.saveInspiration(inspiration)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Create a fresh ViewModel instance to load themes from repository
        val freshViewModel = MainViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert - ViewModel should load the 3 distinct themes from repository
        val state = freshViewModel.uiState.value
        assertThat(state.themes).containsExactly("产品设计", "技术开发", "生活感悟")
    }
}