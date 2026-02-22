package top.alanpu.sparklenote.app.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for InspirationCard component.
 * Tests card display and user interactions.
 */
class InspirationCardTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `inspirationCard_displaysContentAndTheme()`() {
        // Arrange
        val testContent = "This is a test inspiration about mobile app design"
        val testTheme = "产品设计"
        val testTime = "2小时前"
        
        composeTestRule.setContent {
            InspirationCard(
                content = testContent,
                themeName = testTheme,
                createdAtText = testTime,
                onClick = {},
                onDelete = {}
            )
        }
        
        // Assert
        composeTestRule.onNodeWithText(testContent).assertExists()
        composeTestRule.onNodeWithText(testTheme).assertExists()
        composeTestRule.onNodeWithText(testTime).assertExists()
    }
    
    @Test
    fun `inspirationCard_whenClicked_triggersOnClickCallback()`() {
        // Arrange
        var clicked = false
        
        composeTestRule.setContent {
            InspirationCard(
                content = "Test content",
                themeName = "测试主题",
                createdAtText = "1小时前",
                onClick = { clicked = true },
                onDelete = {}
            )
        }
        
        // Act
        composeTestRule.onNodeWithText("Test content").performClick()
        
        // Assert
        assert(clicked)
    }
    
    @Test
    fun `inspirationCard_withLongContent_showsTruncatedText()`() {
        // Arrange
        val longContent = "This is a very long inspiration content that should be truncated in the card display to maintain consistent card height and improve visual presentation."
        
        composeTestRule.setContent {
            InspirationCard(
                content = longContent,
                themeName = "测试主题",
                createdAtText = "刚刚",
                onClick = {},
                onDelete = {}
            )
        }
        
        // Assert - Should display truncated content
        composeTestRule.onNodeWithText(longContent.substring(0, 50) + "...").assertExists()
    }
    
    @Test
    fun `inspirationCard_displaysEmojiIcon()`() {
        // Arrange
        composeTestRule.setContent {
            InspirationCard(
                content = "Test inspiration",
                themeName = "产品设计",
                createdAtText = "30分钟前",
                onClick = {},
                onDelete = {}
            )
        }
        
        // Assert - Should show lightbulb emoji for product design theme
        composeTestRule.onNodeWithText("💡").assertExists()
    }
}