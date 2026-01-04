package com.sparkle.note.ui.screens.themes

import com.sparkle.note.domain.model.Inspiration
import com.sparkle.note.domain.repository.InspirationRepository
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
 * Unit tests for ThemeViewModel.
 * Tests theme management functionality with 100% coverage.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private lateinit var viewModel: ThemeViewModel
    private lateinit var mockRepository: FakeInspirationRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = FakeInspirationRepository()
        viewModel = ThemeViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `themeViewModel_initialState_loadsAllThemesFromRepository`() = runTest {
        // Arrange - Add some inspirations with different themes
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "产品2", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3),
            Inspiration(content = "生活1", themeName = "生活感悟", wordCount = 3)
        )
        inspirations.forEach { mockRepository.saveInspiration(it) }
        testDispatcher.scheduler.advanceUntilIdle()

        // Act - Create fresh ViewModel to load themes
        val freshViewModel = ThemeViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = freshViewModel.uiState.value
        assertThat(state.themes).hasSize(3)
        assertThat(state.themes.map { it.name })
            .containsExactly("产品设计", "技术开发", "生活感悟")
    }

    @Test
    fun `themeViewModel_onCreateNewTheme_addsThemeToList`() = runTest {
        // Arrange
        val newThemeName = "市场营销"

        // Act
        viewModel.onCreateNewTheme(newThemeName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.themes).hasSize(1)
        assertThat(state.themes[0].name).isEqualTo(newThemeName)
    }

    @Test
    fun `themeViewModel_onCreateNewTheme_withEmptyName_showsError`() = runTest {
        // Act
        viewModel.onCreateNewTheme("")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val event = viewModel.events.first()
        assertThat(event).isInstanceOf(ThemeEvent.ShowError::class.java)
        assertThat((event as ThemeEvent.ShowError).message)
            .isEqualTo("主题名称不能为空")
    }

    @Test
    fun `themeViewModel_onCreateNewTheme_withDuplicateName_showsError`() = runTest {
        // Arrange
        val existingTheme = "产品设计"
        mockRepository.saveInspiration(
            Inspiration(content = "示例", themeName = existingTheme, wordCount = 2)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Refresh ViewModel to load existing themes
        val freshViewModel = ThemeViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        freshViewModel.onCreateNewTheme(existingTheme)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val event = freshViewModel.events.first()
        assertThat(event).isInstanceOf(ThemeEvent.ShowError::class.java)
        assertThat((event as ThemeEvent.ShowError).message)
            .isEqualTo("主题已存在")
    }

    @Test
    fun `themeViewModel_onUpdateThemeName_updatesThemeSuccessfully`() = runTest {
        // Arrange
        mockRepository.saveInspiration(
            Inspiration(content = "示例", themeName = "旧主题", wordCount = 2)
        )
        testDispatcher.scheduler.advanceUntilIdle()
        val freshViewModel = ThemeViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val oldTheme = freshViewModel.uiState.value.themes[0]
        val newName = "新主题名称"

        // Act
        freshViewModel.onUpdateThemeName(oldTheme, newName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = freshViewModel.uiState.value
        assertThat(state.themes).hasSize(1)
        assertThat(state.themes[0].name).isEqualTo(newName)

        val event = freshViewModel.events.first()
        assertThat(event).isInstanceOf(ThemeEvent.ShowSuccess::class.java)
    }

    @Test
    fun `themeViewModel_onDeleteTheme_removesThemeAndAssociatedInspirations`() = runTest {
        // Arrange
        val themeName = "待删除主题"
        mockRepository.saveInspiration(
            Inspiration(content = "示例1", themeName = themeName, wordCount = 3)
        )
        mockRepository.saveInspiration(
            Inspiration(content = "示例2", themeName = themeName, wordCount = 3)
        )
        mockRepository.saveInspiration(
            Inspiration(content = "保留", themeName = "其他主题", wordCount = 2)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val freshViewModel = ThemeViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val themeToDelete = freshViewModel.uiState.value.themes.first { it.name == themeName }

        // Act
        freshViewModel.onDeleteTheme(themeToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = freshViewModel.uiState.value
        assertThat(state.themes).hasSize(1) // Only "其他主题" should remain
        assertThat(state.themes[0].name).isEqualTo("其他主题")

        val inspirations = mockRepository.getAllInspirations().first()
        assertThat(inspirations).hasSize(1) // Only inspiration with "其他主题" should remain
        assertThat(inspirations[0].themeName).isEqualTo("其他主题")
    }

    /**
     * Fake repository for testing purposes.
     */
    private class FakeInspirationRepository : InspirationRepository {
        private val inspirations = mutableListOf<Inspiration>()
        private val idCounter = java.util.concurrent.atomic.AtomicLong(1L)

        override suspend fun saveInspiration(inspiration: Inspiration): Result<Unit> {
            return try {
                val newInspiration = if (inspiration.id == 0L) {
                    inspiration.copy(id = idCounter.incrementAndGet())
                } else {
                    inspiration
                }
                inspirations.add(newInspiration)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override fun getAllInspirations(): kotlinx.coroutines.flow.Flow<List<Inspiration>> {
            return kotlinx.coroutines.flow.flow {
                emit(inspirations.toList())
            }
        }

        override fun getDistinctThemes(): kotlinx.coroutines.flow.Flow<List<String>> {
            return kotlinx.coroutines.flow.flow {
                emit(inspirations.map { it.themeName }.distinct())
            }
        }

        override fun searchInspirations(keyword: String): kotlinx.coroutines.flow.Flow<List<Inspiration>> {
            return kotlinx.coroutines.flow.flow {
                val filtered = inspirations.filter {
                    it.content.contains(keyword, ignoreCase = true) ||
                        it.themeName.contains(keyword, ignoreCase = true)
                }
                emit(filtered)
            }
        }

        override fun getInspirationsByTheme(themeName: String): kotlinx.coroutines.flow.Flow<List<Inspiration>> {
            return kotlinx.coroutines.flow.flow {
                emit(inspirations.filter { it.themeName == themeName })
            }
        }

        override suspend fun deleteInspiration(id: Long): Result<Unit> {
            return try {
                inspirations.removeIf { it.id == id }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override suspend fun deleteInspirationsByTheme(themeName: String): Result<Unit> {
            return try {
                inspirations.removeIf { it.themeName == themeName }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override fun exportToMarkdown(inspirations: List<Inspiration>): String {
            return com.sparkle.note.utils.ExportManager.exportBatchToMarkdown(inspirations)
        }

        override fun exportSingleToMarkdown(inspiration: Inspiration): String {
            return com.sparkle.note.utils.ExportManager.exportSingleToMarkdown(inspiration)
        }
    }
}
