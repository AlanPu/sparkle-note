package com.sparkle.note.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sparkle.note.ui.screens.backup.BackupManagementScreen
import com.sparkle.note.ui.screens.batch.BatchOperationScreen
import com.sparkle.note.ui.screens.main.EnhancedMainScreen
import com.sparkle.note.ui.screens.search.AdvancedSearchScreen
import com.sparkle.note.ui.screens.theme.ThemeManagementScreen

/**
 * Navigation graph for the Sparkle Note application.
 * Integrates all Day 3 advanced features with proper navigation.
 */
@Composable
fun SparkleNoteNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Main screen with navigation drawer
        composable("main") {
            EnhancedMainScreen(navController = navController)
        }
        
        // Backup management screen
        composable("backup") {
            BackupManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Batch operations screen
        composable("batch") {
            BatchOperationScreen(
                onNavigateBack = { navController.popBackStack() },
                onInspirationClick = { inspirationId ->
                    // Handle inspiration detail navigation if needed
                }
            )
        }
        
        // Advanced search screen
        composable("search") {
            AdvancedSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onInspirationClick = { inspirationId ->
                    // Handle inspiration detail navigation if needed
                }
            )
        }
        
        // Theme management screen
        composable("theme") {
            ThemeManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}