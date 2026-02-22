package top.alanpu.sparklenote.app.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import top.alanpu.sparklenote.app.data.entity.InspirationEntity
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
    private lateinit var dao: top.alanpu.sparklenote.app.data.database.dao.InspirationDao
    
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
    fun insertAndRetrieveInspiration() = runBlocking {
        // Given
        val inspiration = InspirationEntity(
            content = "Test inspiration",
            themeName = "Test theme",
            createdAt = System.currentTimeMillis()
        )
        
        // When
        dao.insertInspiration(inspiration)
        val allInspirations = dao.getAllInspirations().first()
        
        // Then
        Truth.assertThat(allInspirations).hasSize(1)
        Truth.assertThat(allInspirations[0].content).isEqualTo("Test inspiration")
        Truth.assertThat(allInspirations[0].themeName).isEqualTo("Test theme")
    }
    
    @Test
    fun deleteInspiration() = runBlocking {
        // Given
        val inspiration = InspirationEntity(
            content = "Test inspiration",
            themeName = "Test theme",
            createdAt = System.currentTimeMillis()
        )
        dao.insertInspiration(inspiration)
        
        // When
        val insertedInspiration = dao.getAllInspirations().first()[0]
        dao.deleteInspiration(insertedInspiration.id)
        val allInspirations = dao.getAllInspirations().first()
        
        // Then
        Truth.assertThat(allInspirations).isEmpty()
    }
    
    @Test
    fun searchInspirationsByContent() = runBlocking {
        // Given
        val inspiration1 = InspirationEntity(
            content = "Test inspiration one",
            themeName = "Test theme",
            createdAt = System.currentTimeMillis()
        )
        val inspiration2 = InspirationEntity(
            content = "Another inspiration",
            themeName = "Test theme",
            createdAt = System.currentTimeMillis()
        )
        dao.insertInspiration(inspiration1)
        dao.insertInspiration(inspiration2)
        
        // When
        val searchResults = dao.searchInspirations("Test").first()
        
        // Then
        Truth.assertThat(searchResults).hasSize(1)
        Truth.assertThat(searchResults[0].content).isEqualTo("Test inspiration one")
    }
    
    @Test
    fun getInspirationsByTheme() = runBlocking {
        // Given
        val inspiration1 = InspirationEntity(
            content = "Test inspiration one",
            themeName = "Theme A",
            createdAt = System.currentTimeMillis()
        )
        val inspiration2 = InspirationEntity(
            content = "Test inspiration two",
            themeName = "Theme B",
            createdAt = System.currentTimeMillis()
        )
        dao.insertInspiration(inspiration1)
        dao.insertInspiration(inspiration2)
        
        // When
        val themeAInspirations = dao.getInspirationsByTheme("Theme A").first()
        
        // Then
        Truth.assertThat(themeAInspirations).hasSize(1)
        Truth.assertThat(themeAInspirations[0].themeName).isEqualTo("Theme A")
    }
}