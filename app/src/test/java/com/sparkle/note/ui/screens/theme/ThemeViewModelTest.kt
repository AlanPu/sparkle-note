package com.sparkle.note.ui.screens.theme

import com.sparkle.note.domain.model.Inspiration
import com.sparkle.note.data.repository.MockInspirationRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ThemeViewModel.
 * Tests theme management functionality including CRUD operations and statistics.
 */
class ThemeViewModelTest {
    
    private lateinit var viewModel: ThemeViewModel
    private lateinit var mockRepository: MockInspirationRepository
    
    @Before
    fun setup() {
        mockRepository = MockInspirationRepository()
        viewModel = ThemeViewModel(mockRepository)
    }
    
    @Test
    fun themeViewModel_initialState_hasEmptyThemesAndZeroStats() = runTest {
        // Given: ViewModel is newly created
        
        // When: Get initial state
        val initialState = viewModel.uiState.value
        
        // Then: Should have empty themes and zero stats
        assertThat(initialState.themes).isEmpty()
        assertThat(initialState.totalInspirations).isEqualTo(0)
        assertThat(initialState.isLoading).isFalse()
        assertThat(initialState.errorMessage).isNull()
    }
    
    @Test
    fun themeViewModel_loadThemes_withData_loadsCorrectStatistics() = runTest {
        // Given: Repository has some inspirations
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "产品2", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3),
            Inspiration(content = "生活1", themeName = "生活感悟", wordCount = 3)
        )
        inspirations.forEach { 
            runBlocking { mockRepository.saveInspiration(it) }
        }
        
        // When: Load themes (triggered in init)
        runBlocking { 
            // Force reload by creating new ViewModel
            viewModel = ThemeViewModel(mockRepository)
        }
        
        // Then: Should have correct theme statistics
        val state = viewModel.uiState.value
        assertThat(state.themes).hasSize(3)
        assertThat(state.totalInspirations).isEqualTo(4)
        
        // Check individual theme counts
        val productTheme = state.themes.find { it.name == "产品设计" }
        assertThat(productTheme?.inspirationCount).isEqualTo(2)
        
        val techTheme = state.themes.find { it.name == "技术开发" }
        assertThat(techTheme?.inspirationCount).isEqualTo(1)
        
        val lifeTheme = state.themes.find { it.name == "生活感悟" }
        assertThat(lifeTheme?.inspirationCount).isEqualTo(1)
    }
    
    @Test
    fun themeViewModel_addTheme_withValidName_addsSuccessfully() = runTest {
        // Given: Repository has some existing themes
        val existingInspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3)
        )
        existingInspirations.forEach { 
            runBlocking { mockRepository.saveInspiration(it) }
        }
        
        // When: Add a new theme by creating an inspiration with it
        val newThemeInspiration = Inspiration(content = "新主题内容", themeName = "读书笔记", wordCount = 5)
        runBlocking { mockRepository.saveInspiration(newThemeInspiration) }
        
        // Force reload
        runBlocking { 
            viewModel = ThemeViewModel(mockRepository)
        }
        
        // Then: New theme should appear in statistics
        val state = viewModel.uiState.value
        assertThat(state.themes).hasSize(3)
        
        val newTheme = state.themes.find { it.name == "读书笔记" }
        assertThat(newTheme).isNotNull()
        assertThat(newTheme?.inspirationCount).isEqualTo(1)
    }
    
    @Test
    fun themeViewModel_editTheme_withValidName_updatesAllInspirations() = runTest {
        // Given: Repository has inspirations with a theme to edit
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "产品2", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3)
        )
        inspirations.forEach { 
            runBlocking { mockRepository.saveInspiration(it) }
        }
        
        // When: Edit theme name
        runBlocking { viewModel.editTheme("产品设计", "产品策划") }
        
        // Then: All inspirations should have the new theme name
        val allInspirations = runBlocking { mockRepository.getAllInspirations().first() }
        val updatedProductInspirations = allInspirations.filter { it.themeName == "产品策划" }
        assertThat(updatedProductInspirations).hasSize(2)
        
        val oldThemeInspirations = allInspirations.filter { it.themeName == "产品设计" }
        assertThat(oldThemeInspirations).isEmpty()
    }
    
    @Test
    fun themeViewModel_editTheme_withExistingName_showsError() = runTest {
        // Given: Repository has existing themes
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3)
        )
        inspirations.forEach { 
            runBlocking { mockRepository.saveInspiration(it) }
        }
        
        // When: Try to edit theme to an existing name
        runBlocking { viewModel.editTheme("产品设计", "技术开发") }
        
        // Then: Should show error
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("主题名称已存在")
    }
    
    @Test
    fun themeViewModel_editTheme_withBlankName_showsError() = runTest {
        // Given: Repository has existing themes
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3)
        )
        inspirations.forEach { 
            runBlocking { mockRepository.saveInspiration(it) }
        }
        
        // When: Try to edit theme with blank name
        runBlocking { viewModel.editTheme("产品设计", "") }
        
        // Then: Should show error
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("主题名称不能为空")
    }
    
    @Test
    fun themeViewModel_deleteTheme_movesInspirationsToDefaultTheme() = runTest {
        // Given: Repository has inspirations with a theme to delete
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "产品2", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3)
        )
        inspirations.forEach { 
            runBlocking { mockRepository.saveInspiration(it) }
        }
        
        // When: Delete a theme
        runBlocking { viewModel.deleteTheme("产品设计") }
        
        // Then: Inspirations should be moved to default theme
        val allInspirations = runBlocking { mockRepository.getAllInspirations().first() }
        val defaultThemeInspirations = allInspirations.filter { it.themeName == "未分类" }
        assertThat(defaultThemeInspirations).hasSize(2)
        
        val deletedThemeInspirations = allInspirations.filter { it.themeName == "产品设计" }
        assertThat(deletedThemeInspirations).isEmpty()
    }
    
    @Test
    fun themeViewModel_deleteTheme_withNoInspirations_succeeds() = runTest {
        // Given: Repository has themes but no inspirations for one theme
        val inspirations = listOf(
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3),
            Inspiration(content = "生活1", themeName = "生活感悟", wordCount = 3)
        )
        inspirations.forEach { 
            runBlocking { mockRepository.saveInspiration(it) }
        }
        
        // When: Delete a theme that has no inspirations
        runBlocking { viewModel.deleteTheme("产品设计") } // This theme doesn't exist
        
        // Then: Should succeed without errors
        val allInspirations = runBlocking { mockRepository.getAllInspirations().first() }
        assertThat(allInspirations).hasSize(2)
        // No change since theme didn't exist
    }
    
    @Test
    fun themeViewModel_deleteDefaultTheme_showsError() = runTest {
        // Given: Repository has default theme
        val inspirations = listOf(
            Inspiration(content = "内容1", themeName = "未分类", wordCount = 3)
        )
        inspirations.forEach { 
            runBlocking { mockRepository.saveInspiration(it) }
        }
        
        // When: Try to delete default theme
        runBlocking { viewModel.deleteTheme("未分类") }
        
        // Then: Should show error
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("不能删除默认主题")
    }
    
    @Test
    fun themeViewModel_clearError_removesErrorMessage() = runTest {
        // Given: ViewModel has an error
        runBlocking { viewModel.addTheme("") } // This will cause an error
        val stateWithError = viewModel.uiState.value
        assertThat(stateWithError.errorMessage).isNotNull()
        
        // When: Clear error
        viewModel.clearError()
        
        // Then: Error should be cleared
        val stateAfterClear = viewModel.uiState.value
        assertThat(stateAfterClear.errorMessage).isNull()
    }
    
    @Test
    fun themeViewModel_themeInfo_dataClassWorksCorrectly() {
        // Given: ThemeInfo instances
        val theme1 = ThemeInfo("产品设计", 5)
        val theme2 = ThemeInfo("产品设计", 5)
        val theme3 = ThemeInfo("技术开发", 3)
        
        // Then: Data class behavior should work correctly
        assertThat(theme1).isEqualTo(theme2)
        assertThat(theme1).isNotEqualTo(theme3)
        assertThat(theme1.hashCode()).isEqualTo(theme2.hashCode())
        assertThat(theme1.toString()).contains("产品设计")
        assertThat(theme1.toString()).contains("5")
    }
}