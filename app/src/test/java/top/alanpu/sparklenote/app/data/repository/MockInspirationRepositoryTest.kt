package top.alanpu.sparklenote.app.data.repository

import top.alanpu.sparklenote.app.domain.model.Inspiration
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MockInspirationRepository.
 * Tests repository operations and data integrity.
 */
class MockInspirationRepositoryTest {
    
    private lateinit var repository: MockInspirationRepository
    
    @Before
    fun setup() {
        repository = MockInspirationRepository()
    }
    
    @Test
    fun `saveInspiration_withValidData_savesSuccessfully()`() = runBlocking {
        // Arrange
        val inspiration = Inspiration(
            content = "这是一个测试灵感",
            themeName = "产品设计",
            wordCount = 7
        )
        
        // Act
        val result = repository.saveInspiration(inspiration)
        
        // Assert
        assertThat(result.isSuccess).isTrue()
        
        val savedInspirations = repository.getAllInspirations().first()
        assertThat(savedInspirations).hasSize(1)
        assertThat(savedInspirations[0].content).isEqualTo("这是一个测试灵感")
        assertThat(savedInspirations[0].themeName).isEqualTo("产品设计")
        assertThat(savedInspirations[0].wordCount).isEqualTo(7)
    }
    
    @Test
    fun `saveInspiration_assignsUniqueIds()`() = runBlocking {
        // Arrange
        val inspiration1 = Inspiration(
            content = "第一个灵感",
            themeName = "产品设计",
            wordCount = 5
        )
        val inspiration2 = Inspiration(
            content = "第二个灵感",
            themeName = "技术开发",
            wordCount = 5
        )
        
        // Act
        repository.saveInspiration(inspiration1)
        repository.saveInspiration(inspiration2)
        
        // Assert
        val savedInspirations = repository.getAllInspirations().first()
        assertThat(savedInspirations).hasSize(2)
        assertThat(savedInspirations[0].id).isNotEqualTo(savedInspirations[1].id)
    }
    
    @Test
    fun `getAllInspirations_returnsAllSavedData()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            Inspiration(content = "灵感1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "灵感2", themeName = "技术开发", wordCount = 3),
            Inspiration(content = "灵感3", themeName = "生活感悟", wordCount = 3)
        )
        
        // Act
        inspirations.forEach { repository.saveInspiration(it) }
        val allInspirations = repository.getAllInspirations().first()
        
        // Assert
        assertThat(allInspirations).hasSize(3)
        assertThat(allInspirations.map { it.content }).containsExactly("灵感1", "灵感2", "灵感3")
        Unit
    }
    
    @Test
    fun `getDistinctThemes_returnsUniqueThemes()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "产品2", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3),
            Inspiration(content = "生活1", themeName = "生活感悟", wordCount = 3)
        )
        
        // Act
        inspirations.forEach { repository.saveInspiration(it) }
        val distinctThemes = repository.getDistinctThemes().first()
        
        // Assert
        assertThat(distinctThemes).hasSize(3)
        assertThat(distinctThemes).containsExactly("产品设计", "技术开发", "生活感悟")
        Unit
    }
    
    @Test
    fun `searchInspirations_findsMatchesInContentAndTheme()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            Inspiration(content = "移动应用设计", themeName = "产品设计", wordCount = 5),
            Inspiration(content = "数据库优化", themeName = "技术开发", wordCount = 4),
            Inspiration(content = "应用架构设计", themeName = "产品设计", wordCount = 6)
        )
        inspirations.forEach { repository.saveInspiration(it) }
        
        // Act
        val searchResults = repository.searchInspirations("设计").first()
        
        // Assert
        assertThat(searchResults).hasSize(2)
        assertThat(searchResults.map { it.content }).containsExactly(
            "移动应用设计",
            "应用架构设计"
        )
        Unit
    }
    
    @Test
    fun `getInspirationsByTheme_returnsOnlyMatchingTheme()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            Inspiration(content = "产品1", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "产品2", themeName = "产品设计", wordCount = 3),
            Inspiration(content = "技术1", themeName = "技术开发", wordCount = 3)
        )
        inspirations.forEach { repository.saveInspiration(it) }
        
        // Act
        val productDesignInspirations = repository.getInspirationsByTheme("产品设计").first()
        
        // Assert
        assertThat(productDesignInspirations).hasSize(2)
        assertThat(productDesignInspirations.map { it.content }).containsExactly("产品1", "产品2")
        Unit
    }
    
    @Test
    fun `deleteInspiration_removesCorrectData()`() = runBlocking {
        // Arrange
        val inspiration1 = Inspiration(content = "要删除的", themeName = "测试", wordCount = 3)
        val inspiration2 = Inspiration(content = "保留的", themeName = "测试", wordCount = 2)
        repository.saveInspiration(inspiration1)
        repository.saveInspiration(inspiration2)
        val savedInspirations = repository.getAllInspirations().first()
        val idToDelete = savedInspirations[0].id
        
        // Act
        val deleteResult = repository.deleteInspiration(idToDelete)
        
        // Assert
        assertThat(deleteResult.isSuccess).isTrue()
        
        val remainingInspirations = repository.getAllInspirations().first()
        assertThat(remainingInspirations).hasSize(1)
        assertThat(remainingInspirations[0].content).isEqualTo("保留的")
    }
    
    @Test
    fun `exportToMarkdown_generatesCorrectFormat()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            Inspiration(
                content = "做一个支持离线使用的灵感笔记App",
                themeName = "产品设计",
                wordCount = 15,
                createdAt = 1234567890000
            ),
            Inspiration(
                content = "优化数据库查询性能",
                themeName = "技术开发",
                wordCount = 10,
                createdAt = 1234567891000
            )
        )
        inspirations.forEach { repository.saveInspiration(it) }
        
        // Act
        val markdown = repository.exportToMarkdown(inspirations)
        
        // Assert
        assertThat(markdown).contains("# 我的灵感笔记")
        assertThat(markdown).contains("## 产品设计（1条）")
        assertThat(markdown).contains("## 技术开发（1条）")
        assertThat(markdown).contains("做一个支持离线使用的灵感笔记App")
        assertThat(markdown).contains("优化数据库查询性能")
    }
    
    @Test
    fun `exportSingleToMarkdown_generatesCorrectFormat()`() {
        // Arrange
        val inspiration = Inspiration(
            content = "做一个支持离线使用的灵感笔记App",
            themeName = "产品设计",
            wordCount = 15,
            createdAt = 1234567890000
        )
        
        // Act
        val markdown = repository.exportSingleToMarkdown(inspiration)
        
        // Assert
        assertThat(markdown).contains("主题: 产品设计")
        assertThat(markdown).contains("字数: 15")
        assertThat(markdown).contains("做一个支持离线使用的灵感笔记App")
    }
}