package top.alanpu.sparklenote.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import top.alanpu.sparklenote.app.ui.screens.backup.BackupManagementScreen
import top.alanpu.sparklenote.app.ui.screens.batch.BatchOperationScreen
import top.alanpu.sparklenote.app.ui.screens.main.EnhancedMainScreen
import top.alanpu.sparklenote.app.ui.screens.search.AdvancedSearchScreen
import top.alanpu.sparklenote.app.ui.screens.theme.ThemeManagementScreen
import top.alanpu.sparklenote.app.ui.screens.settings.ThemeSettingsScreen

/**
 * Navigation graph for Sparkle Note application.
 * Defines all navigation routes and their corresponding screens.
 */
@Composable
fun SparkleNoteNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Main screen
        composable("main") {
            EnhancedMainScreen(navController = navController)
        }
        
        // Advanced search screen
        composable("search") {
            AdvancedSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onInspirationClick = { _ ->
                    // Handle inspiration click in search results
                    navController.popBackStack()
                }
            )
        }
        
        // Batch operation screen
        composable("batch") {
            BatchOperationScreen(
                onNavigateBack = { navController.popBackStack() },
                onInspirationClick = { _ ->
                    // Handle inspiration click in batch operations
                    navController.popBackStack()
                }
            )
        }
        
        // Backup management screen
        composable("backup") {
            BackupManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Theme management screen
        composable("theme") {
            ThemeManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Theme settings screen
        composable("theme_settings") {
            ThemeSettingsScreen(
                navController = navController
            )
        }
    }
}