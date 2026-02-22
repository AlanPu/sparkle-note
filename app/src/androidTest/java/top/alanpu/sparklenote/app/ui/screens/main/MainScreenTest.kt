package top.alanpu.sparklenote.app.ui.screens.main

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for MainScreen component.
 * Tests the complete main screen with all integrated components.
 */
class MainScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `mainScreen_displaysAppTitle()`() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        composeTestRule.onNodeWithText("Sparkle Note").assertExists()
    }
    
    @Test
    fun `mainScreen_displaysQuickRecordSection()`() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        composeTestRule.onNodeWithText("快速记录").assertExists()
        composeTestRule.onNodeWithText("记录你的灵感...").assertExists()
    }
    
    @Test
    fun `mainScreen_showsEmptyStateInitially()`() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        composeTestRule.onNodeWithText("💡").assertExists()
        composeTestRule.onNodeWithText("还没有灵感记录").assertExists()
        composeTestRule.onNodeWithText("开始记录你的第一个灵感吧！").assertExists()
    }
    
    @Test
    fun `mainScreen_canAddInspiration()`() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        // Type content
        composeTestRule.onNodeWithText("记录你的灵感...").performTextInput("这是一个测试灵感")
        
        // Click save
        composeTestRule.onNodeWithText("保存").performClick()
        
        // Verify inspiration appears in list
        composeTestRule.onNodeWithText("这是一个测试灵感").assertExists()
    }
    
    @Test
    fun `mainScreen_canDeleteInspiration()`() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        // Add an inspiration first
        composeTestRule.onNodeWithText("记录你的灵感...").performTextInput("要删除的灵感")
        composeTestRule.onNodeWithText("保存").performClick()
        
        // Wait for it to appear
        composeTestRule.waitForIdle()
        
        // Verify it exists
        composeTestRule.onNodeWithText("要删除的灵感").assertExists()
        
        // Delete it (click on the card)
        composeTestRule.onNodeWithText("要删除的灵感").performClick()
        
        // Verify it's gone (empty state should appear)
        composeTestRule.onNodeWithText("还没有灵感记录").assertExists()
    }
    
    @Test
    fun `mainScreen_displaysThemeChips()`() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        // Default themes should be visible
        composeTestRule.onNodeWithText("未分类").assertExists()
        composeTestRule.onNodeWithText("产品设计").assertExists()
        composeTestRule.onNodeWithText("技术开发").assertExists()
        composeTestRule.onNodeWithText("生活感悟").assertExists()
    }
    
    @Test
    fun `mainScreen_canSelectTheme()`() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        // Click on a theme chip
        composeTestRule.onNodeWithText("产品设计").performClick()
        
        // Add inspiration with selected theme
        composeTestRule.onNodeWithText("记录你的灵感...").performTextInput("产品设计灵感")
        composeTestRule.onNodeWithText("保存").performClick()
        
        // Verify theme is displayed in card
        composeTestRule.onNodeWithText("产品设计").assertExists()
    }
}