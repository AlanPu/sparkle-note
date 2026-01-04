package com.sparkle.note.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * UI tests for QuickRecordSection component.
 * Tests user interactions and component behavior.
 */
@RunWith(JUnit4::class)
class QuickRecordSectionTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `quickRecordSection_withEmptyInput_showsPlaceholderText()`() {
        // Arrange
        composeTestRule.setContent {
            QuickRecordSection(
                content = "",
                onContentChange = {},
                selectedTheme = "未分类",
                onThemeSelect = {},
                onSave = {}
            )
        }
        
        // Assert
        composeTestRule.onNodeWithText("记录你的灵感...").assertExists()
    }
    
    @Test
    fun `quickRecordSection_withContent_enablesSaveButton()`() {
        // Arrange
        composeTestRule.setContent {
            QuickRecordSection(
                content = "Test inspiration content",
                onContentChange = {},
                selectedTheme = "产品设计",
                onThemeSelect = {},
                onSave = {}
            )
        }
        
        // Assert
        composeTestRule.onNodeWithText("保存").assertIsEnabled()
    }
    
    @Test
    fun `quickRecordSection_withEmptyContent_disablesSaveButton()`() {
        // Arrange
        composeTestRule.setContent {
            QuickRecordSection(
                content = "",
                onContentChange = {},
                selectedTheme = "未分类",
                onThemeSelect = {},
                onSave = {}
            )
        }
        
        // Assert
        composeTestRule.onNodeWithText("保存").assertIsNotEnabled()
    }
    
    @Test
    fun `quickRecordSection_displaysSelectedTheme()`() {
        // Arrange
        val testTheme = "产品设计"
        
        composeTestRule.setContent {
            QuickRecordSection(
                content = "Test content",
                onContentChange = {},
                selectedTheme = testTheme,
                onThemeSelect = {},
                onSave = {}
            )
        }
        
        // Assert
        composeTestRule.onNodeWithText(testTheme).assertExists()
    }
    
    @Test
    fun `quickRecordSection_whenSaveClicked_triggersOnSaveCallback()`() {
        // Arrange
        var saveClicked = false
        
        composeTestRule.setContent {
            QuickRecordSection(
                content = "Test content",
                onContentChange = {},
                selectedTheme = "测试主题",
                onThemeSelect = {},
                onSave = { saveClicked = true }
            )
        }
        
        // Act
        composeTestRule.onNodeWithText("保存").performClick()
        
        // Assert
        assert(saveClicked)
    }
    
    @Test
    fun `quickRecordSection_whenContentChanged_triggersOnContentChangeCallback()`() {
        // Arrange
        var contentChanged = ""
        
        composeTestRule.setContent {
            QuickRecordSection(
                content = "Initial content",
                onContentChange = { contentChanged = it },
                selectedTheme = "测试主题",
                onThemeSelect = {},
                onSave = {}
            )
        }
        
        // Act
        composeTestRule.onNodeWithText("Initial content").performTextInput("New content")
        
        // Assert
        assert(contentChanged.isNotEmpty())
    }
}