package com.sparkle.note

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.sparkle.note.ui.navigation.SparkleNoteNavGraph
import com.sparkle.note.ui.theme.SparkleNoteTheme
import com.sparkle.note.ui.theme.ThemeManager
import com.sparkle.note.ui.theme.ThemeStyle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity for Sparkle Note application.
 * Entry point for the Android application with Hilt injection.
 * Supports dynamic theme switching based on user preferences.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Observe theme preferences
            val currentThemeStyle by themeManager.themeStyle.collectAsState(initial = ThemeStyle.NORDIC)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = true)
            
            SparkleNoteTheme(
                themeStyle = currentThemeStyle,
                darkTheme = isDarkMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SparkleNoteNavGraph(navController = navController)
                }
            }
        }
    }
}