package com.sparkle.note.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for the Inspiration domain model.
 * Tests validation logic and business rules.
 */
class InspirationTest {
    
    @Test
    fun `validateContent_withValidContent_returnsValid()`() {
        // Arrange
        val inspiration = Inspiration(
            content = "This is a valid inspiration content",
            themeName = "Test Theme",
            wordCount = 6
        )
        
        // Act
        val result = inspiration.validateContent()
        
        // Assert
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }
    
    @Test
    fun `validateContent_withEmptyContent_returnsEmpty()`() {
        // Arrange
        val inspiration = Inspiration(
            content = "",
            themeName = "Test Theme",
            wordCount = 0
        )
        
        // Act
        val result = inspiration.validateContent()
        
        // Assert
        assertThat(result).isInstanceOf(ValidationResult.Empty::class.java)
    }
    
    @Test
    fun `validateContent_withBlankContent_returnsEmpty()`() {
        // Arrange
        val inspiration = Inspiration(
            content = "   ",
            themeName = "Test Theme",
            wordCount = 0
        )
        
        // Act
        val result = inspiration.validateContent()
        
        // Assert
        assertThat(result).isInstanceOf(ValidationResult.Empty::class.java)
    }
    
    @Test
    fun `validateContent_withContentExceedingMaxLength_returnsTooLong()`() {
        // Arrange
        val longContent = "a".repeat(Inspiration.MAX_CONTENT_LENGTH + 1)
        val inspiration = Inspiration(
            content = longContent,
            themeName = "Test Theme",
            wordCount = longContent.length
        )
        
        // Act
        val result = inspiration.validateContent()
        
        // Assert
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }
    
    @Test
    fun `validateContent_withContentAtMaxLength_returnsValid()`() {
        // Arrange
        val maxContent = "a".repeat(Inspiration.MAX_CONTENT_LENGTH)
        val inspiration = Inspiration(
            content = maxContent,
            themeName = "Test Theme",
            wordCount = maxContent.length
        )
        
        // Act
        val result = inspiration.validateContent()
        
        // Assert
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }
    
    @Test
    fun `inspiration_withDefaultValues_createsCorrectly()`() {
        // Arrange & Act
        val inspiration = Inspiration(
            content = "Test content",
            themeName = "Test Theme",
            wordCount = 2
        )
        
        // Assert
        assertThat(inspiration.id).isEqualTo(0L)
        assertThat(inspiration.content).isEqualTo("Test content")
        assertThat(inspiration.themeName).isEqualTo("Test Theme")
        assertThat(inspiration.wordCount).isEqualTo(2)
        assertThat(inspiration.createdAt).isGreaterThan(0L)
    }
}