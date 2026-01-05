package com.sparkle.note.ui.screens.theme

import com.sparkle.note.domain.model.Theme
import com.sparkle.note.data.repository.MockThemeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ThemeViewModel using new theme architecture.
 * Tests theme management functionality with independent theme entities.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {
    
    private lateinit var viewModel: ThemeViewModel
    private lateinit var mockThemeRepository: MockThemeRepository
    
    @Before
    fun setup() {
        mockThemeRepository = MockThemeRepository()
        viewModel = ThemeViewModel(mockThemeRepository)
    }
    
    @Test
    fun themeViewModel_initialState_loadsDefaultThemes() = runTest {
        // Given: ViewModel is newly created
        
        // When: Get initial state
        val initialState = viewModel.uiState.value
        
        // Then: Should have default themes loaded
        assertThat(initialState.themes).isNotEmpty()
        assertThat(initialState.themes.map { it.name }).contains("未分类")
        assertThat(initialState.totalInspirations).isEqualTo(0)
        assertThat(initialState.isLoading).isFalse()
        assertThat(initialState.errorMessage).isNull()
    }
    
    @Test
    fun themeViewModel_addTheme_withValidName_createsNewTheme() = runTest {
        // Given: ViewModel with existing themes
        val newThemeName = "读书笔记"
        
        // When: Add a new theme
        viewModel.addTheme(newThemeName)
        
        // Then: New theme should be created
        val state = viewModel.uiState.value
        assertThat(state.themes.map { it.name }).contains(newThemeName)
        assertThat(state.successMessage).contains("读书笔记创建成功")
        assertThat(state.errorMessage).isNull()
    }
    
    @Test
    fun themeViewModel_addTheme_withEmptyName_showsError() = runTest {
        // Given: ViewModel ready to add theme
        
        // When: Try to add theme with empty name
        viewModel.addTheme("")
        
        // Then: Should show error
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("主题名称不能为空")
        assertThat(state.successMessage).isNull()
    }
    
    @Test
    fun themeViewModel_addTheme_withDuplicateName_showsError() = runTest {
        // Given: Existing theme
        val existingThemeName = "产品设计"
        
        // When: Try to add duplicate theme
        viewModel.addTheme(existingThemeName)
        
        // Then: Should show error
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("主题已存在")
        assertThat(state.successMessage).isNull()
    }
    
    @Test
    fun themeViewModel_editTheme_withValidName_updatesTheme() = runTest {
        // Given: Existing theme
        val oldName = "产品设计"
        val newName = "产品策划"
        
        // When: Edit theme name
        viewModel.editTheme(oldName, newName)
        
        // Then: Theme should be updated
        val state = viewModel.uiState.value
        assertThat(state.themes.map { it.name }).contains(newName)
        assertThat(state.themes.map { it.name }).doesNotContain(oldName)
        assertThat(state.successMessage).contains("已更新为")
    }
    
    @Test
    fun themeViewModel_editTheme_withEmptyName_showsError() = runTest {
        // Given: Existing theme
        
        // When: Try to edit with empty name
        viewModel.editTheme("产品设计", "")
        
        // Then: Should show error
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("主题名称不能为空")
    }
    
    @Test
    fun themeViewModel_editTheme_withDuplicateName_showsError() = runTest {
        // Given: Multiple existing themes
        
        // When: Try to edit to existing name
        viewModel.editTheme("产品设计", "技术开发")
        
        // Then: Should show error
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("主题名称已存在")
    }
    
    @Test
    fun themeViewModel_deleteTheme_succeeds() = runTest {
        // Given: Existing theme
        val themeToDelete = "产品设计"
        
        // When: Delete theme
        viewModel.deleteTheme(themeToDelete)
        
        // Then: Theme should be deleted
        val state = viewModel.uiState.value
        assertThat(state.themes.map { it.name }).doesNotContain(themeToDelete)
        assertThat(state.successMessage).contains("已删除")
    }
    
    @Test
    fun themeViewModel_deleteDefaultTheme_showsError() = runTest {
        // Given: Default theme exists
        
        // When: Try to delete default theme
        viewModel.deleteTheme("未分类")
        
        // Then: Should show error
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("不能删除默认主题")
    }
    
    @Test
    fun themeViewModel_changeSortOrder_updatesThemeOrder() = runTest {
        // Given: Multiple themes exist
        viewModel.addTheme("A主题")
        viewModel.addTheme("C主题")
        viewModel.addTheme("B主题")
        
        // When: Change sort order
        viewModel.changeSortOrder(ThemeSortBy.NAME)
        
        // Then: Themes should be sorted by name
        val state = viewModel.uiState.value
        val themeNames = state.themes.map { it.name }
        assertThat(themeNames).isEqualTo(themeNames.sorted())
    }
    
    @Test
    fun themeViewModel_clearError_removesErrorMessage() = runTest {
        // Given: ViewModel with error state
        viewModel.addTheme("") // This will cause error
        
        // When: Clear error
        viewModel.clearError()
        
        // Then: Error should be cleared
        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isNull()
    }
    
    @Test
    fun themeViewModel_refreshThemes_updatesThemeList() = runTest {
        // Given: ViewModel with themes
        val initialThemeCount = viewModel.uiState.value.themes.size
        
        // When: Refresh themes
        viewModel.refreshThemes()
        
        // Then: Theme list should be refreshed (same size, but reloaded)
        val state = viewModel.uiState.value
        assertThat(state.themes.size).isEqualTo(initialThemeCount)
    }
    
    @Test
    fun themeViewModel_themeInfo_dataClassWorksCorrectly() {
        // Given: ThemeInfo instances
        val theme1 = ThemeInfo(
            name = "产品设计",
            icon = "💡",
            color = 0xFF4A90E2,
            description = "",
            inspirationCount = 5,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
        val theme2 = ThemeInfo(
            name = "产品设计",
            icon = "💡",
            color = 0xFF4A90E2,
            description = "",
            inspirationCount = 5,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
        val theme3 = ThemeInfo(
            name = "技术开发",
            icon = "💡",
            color = 0xFF4A90E2,
            description = "",
            inspirationCount = 3,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
        
        // Then: Data class behavior should work correctly
        assertThat(theme1).isEqualTo(theme2)
        assertThat(theme1).isNotEqualTo(theme3)
        assertThat(theme1.hashCode()).isEqualTo(theme2.hashCode())
        assertThat(theme1.toString()).contains("产品设计")
        assertThat(theme1.toString()).contains("5")
    }
}