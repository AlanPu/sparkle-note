package com.sparkle.note.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material Theme configuration for Sparkle Note app.
 * Implements Nordic design principles with dark theme as default.
 */

private val DarkColorScheme = darkColorScheme(
    primary = NordicBlue,
    onPrimary = Color.White,
    secondary = NordicGreen,
    onSecondary = Color.Black,
    background = NordicBackgroundDark,
    onBackground = NordicTextPrimaryDark,
    surface = NordicSurfaceDark,
    onSurface = NordicTextPrimaryDark,
    surfaceVariant = NordicDividerDark,
    onSurfaceVariant = NordicTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = NordicBlue,
    onPrimary = Color.White,
    secondary = NordicGreen,
    onSecondary = Color.Black,
    background = NordicBackgroundLight,
    onBackground = NordicTextPrimaryLight,
    surface = NordicSurfaceLight,
    onSurface = NordicTextPrimaryLight,
    surfaceVariant = NordicDividerLight,
    onSurfaceVariant = NordicTextSecondaryLight
)

/**
 * Main theme composable for the Sparkle Note application.
 * Automatically switches between light and dark themes based on system preference.
 */
@Composable
fun SparkleNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}