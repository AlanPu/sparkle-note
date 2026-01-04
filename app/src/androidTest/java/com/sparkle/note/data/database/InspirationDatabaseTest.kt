package com.sparkle.note.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.sparkle.note.data.entity.InspirationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for InspirationDatabase.
 * Tests database operations and data integrity.
 */
@RunWith(AndroidJUnit4::class)
class InspirationDatabaseTest {
    
    private lateinit var database: InspirationDatabase
    private lateinit var dao: com.sparkle.note.data.database.dao.InspirationDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            InspirationDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.inspirationDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `insertAndRetrieveInspiration_insertsCorrectlyAndRetrievesData()`() = runBlocking {
        // Arrange
        val inspiration = InspirationEntity(
            content = "这是一个测试灵感",
            theme_name = "产品设计",
            word_count = 7
        )
        
        // Act
        val id = dao.insert(inspiration)
        val retrieved = dao.getInspirationById(id)
        
        // Assert
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.content).isEqualTo("这是一个测试灵感")
        assertThat(retrieved?.theme_name).isEqualTo("产品设计")
        assertThat(retrieved?.word_count).isEqualTo(7)
        assertThat(retrieved?.created_at).isGreaterThan(0)
    }
    
    @Test
    fun `getAllInspirations_returnsAllDataOrderedByCreatedAtDesc()`() = runBlocking {
        // Arrange
        val inspiration1 = InspirationEntity(
            content = "第一个灵感",
            theme_name = "产品设计",
            word_count = 5,
            created_at = System.currentTimeMillis() - 1000
        )
        val inspiration2 = InspirationEntity(
            content = "第二个灵感",
            theme_name = "技术开发",
            word_count = 5,
            created_at = System.currentTimeMillis()
        )
        
        // Act
        dao.insert(inspiration1)
        dao.insert(inspiration2)
        val allInspirations = dao.getAllInspirations().first()
        
        // Assert
        assertThat(allInspirations).hasSize(2)
        assertThat(allInspirations[0].content).isEqualTo("第二个灵感") // Newest first
        assertThat(allInspirations[1].content).isEqualTo("第一个灵感")
    }
    
    @Test
    fun `getDistinctThemes_returnsUniqueThemeNames()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            InspirationEntity(content = "灵感1", theme_name = "产品设计", word_count = 3),
            InspirationEntity(content = "灵感2", theme_name = "技术开发", word_count = 3),
            InspirationEntity(content = "灵感3", theme_name = "产品设计", word_count = 3),
            InspirationEntity(content = "灵感4", theme_name = "生活感悟", word_count = 3)
        )
        
        // Act
        inspirations.forEach { dao.insert(it) }
        val distinctThemes = dao.getDistinctThemes().first()
        
        // Assert
        assertThat(distinctThemes).hasSize(3)
        assertThat(distinctThemes).containsExactly("产品设计", "技术开发", "生活感悟")
    }
    
    @Test
    fun `searchInspirations_findsMatchesInContentAndTheme()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            InspirationEntity(content = "移动应用设计", theme_name = "产品设计", word_count = 5),
            InspirationEntity(content = "数据库优化", theme_name = "技术开发", word_count = 4),
            InspirationEntity(content = "应用架构设计", theme_name = "产品设计", word_count = 6)
        )
        
        // Act
        inspirations.forEach { dao.insert(it) }
        val searchResults = dao.searchInspirations("设计").first()
        
        // Assert
        assertThat(searchResults).hasSize(2)
        assertThat(searchResults.map { it.content }).containsExactly(
            "应用架构设计",
            "移动应用设计"
        )
    }
    
    @Test
    fun `getInspirationsByTheme_returnsOnlyMatchingTheme()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            InspirationEntity(content = "灵感1", theme_name = "产品设计", word_count = 3),
            InspirationEntity(content = "灵感2", theme_name = "技术开发", word_count = 3),
            InspirationEntity(content = "灵感3", theme_name = "产品设计", word_count = 3)
        )
        
        // Act
        inspirations.forEach { dao.insert(it) }
        val productDesignInspirations = dao.getInspirationsByTheme("产品设计").first()
        
        // Assert
        assertThat(productDesignInspirations).hasSize(2)
        assertThat(productDesignInspirations.map { it.content }).containsExactly("灵感1", "灵感3")
    }
    
    @Test
    fun `deleteById_removesCorrectInspiration()`() = runBlocking {
        // Arrange
        val inspiration1 = InspirationEntity(content = "要删除的", theme_name = "测试", word_count = 3)
        val inspiration2 = InspirationEntity(content = "保留的", theme_name = "测试", word_count = 2)
        val id1 = dao.insert(inspiration1)
        dao.insert(inspiration2)
        
        // Act
        dao.deleteById(id1)
        val remainingInspirations = dao.getAllInspirations().first()
        
        // Assert
        assertThat(remainingInspirations).hasSize(1)
        assertThat(remainingInspirations[0].content).isEqualTo("保留的")
    }
    
    @Test
    fun `updateInspiration_updatesDataCorrectly()`() = runBlocking {
        // Arrange
        val original = InspirationEntity(
            content = "原始内容",
            theme_name = "原始主题",
            word_count = 4
        )
        val id = dao.insert(original)
        val updated = original.copy(
            id = id,
            content = "更新后的内容",
            theme_name = "更新主题",
            word_count = 6
        )
        
        // Act
        dao.update(updated)
        val retrieved = dao.getInspirationById(id)
        
        // Assert
        assertThat(retrieved?.content).isEqualTo("更新后的内容")
        assertThat(retrieved?.theme_name).isEqualTo("更新主题")
        assertThat(retrieved?.word_count).isEqualTo(6)
    }
    
    @Test
    fun `getInspirationCount_returnsCorrectCount()`() = runBlocking {
        // Arrange
        val inspirations = List(5) { index ->
            InspirationEntity(
                content = "灵感${index + 1}",
                theme_name = "测试",
                word_count = 3
            )
        }
        
        // Act
        inspirations.forEach { dao.insert(it) }
        val count = dao.getInspirationCount()
        
        // Assert
        assertThat(count).isEqualTo(5)
    }
    
    @Test
    fun `getInspirationCountByTheme_returnsCorrectCountForTheme()`() = runBlocking {
        // Arrange
        val inspirations = listOf(
            InspirationEntity(content = "产品1", theme_name = "产品设计", word_count = 3),
            InspirationEntity(content = "产品2", theme_name = "产品设计", word_count = 3),
            InspirationEntity(content = "技术1", theme_name = "技术开发", word_count = 3)
        )
        
        // Act
        inspirations.forEach { dao.insert(it) }
        val productDesignCount = dao.getInspirationCountByTheme("产品设计")
        val techCount = dao.getInspirationCountByTheme("技术开发")
        
        // Assert
        assertThat(productDesignCount).isEqualTo(2)
        assertThat(techCount).isEqualTo(1)
    }
}