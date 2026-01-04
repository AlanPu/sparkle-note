package com.sparkle.note

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sparkle.note.ui.screens.main.EnhancedMainScreen
import com.sparkle.note.ui.screens.main.MainScreen
import com.sparkle.note.ui.theme.SparkleNoteTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for Sparkle Note application.
 * Entry point for the Android application with Hilt injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SparkleNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EnhancedMainScreen()
                }
            }
        }
    }
}