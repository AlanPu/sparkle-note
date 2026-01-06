package com.sparkle.note.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Theme style enumeration for different color schemes
 */
enum class ThemeStyle {
    NORDIC,      // Original Nordic theme
    DEEP_NIGHT,  // Dark mode with neon accents
    MINT_MORNING, // Fresh light theme
    SCHOLAR_BLUE  // Academic blue theme
}

/**
 * Material Theme configuration for Sparkle Note app.
 * Supports multiple theme styles with dark/light variants.
 */

// Nordic Theme (Original)
private val NordicDarkColorScheme = darkColorScheme(
    primary = NordicBlue,
    onPrimary = Color.White,
    secondary = NordicGreen,
    onSecondary = Color.Black,
    background = NordicBackgroundDark,
    onBackground = NordicTextPrimaryDark,
    surface = NordicSurfaceDark,
    onSurface = NordicTextPrimaryDark,
    surfaceVariant = NordicDividerDark,
    onSurfaceVariant = NordicTextSecondaryDark,
    error = NordicError,
    onError = Color.White
)

private val NordicLightColorScheme = lightColorScheme(
    primary = NordicBlue,
    onPrimary = Color.White,
    secondary = NordicGreen,
    onSecondary = Color.Black,
    background = NordicBackgroundLight,
    onBackground = NordicTextPrimaryLight,
    surface = NordicSurfaceLight,
    onSurface = NordicTextPrimaryLight,
    surfaceVariant = NordicDividerLight,
    onSurfaceVariant = NordicTextSecondaryLight,
    error = NordicError,
    onError = Color.White
)

// Deep Night Theme
private val DeepNightDarkColorScheme = darkColorScheme(
    primary = DeepNightColors.Primary,
    onPrimary = DeepNightColors.OnPrimary,
    secondary = DeepNightColors.Secondary,
    onSecondary = DeepNightColors.OnSecondary,
    background = DeepNightColors.Background,
    onBackground = DeepNightColors.OnBackground,
    surface = DeepNightColors.Surface,
    onSurface = DeepNightColors.OnSurface,
    surfaceVariant = DeepNightColors.SurfaceVariant,
    onSurfaceVariant = DeepNightColors.OnSurfaceVariant,
    error = DeepNightColors.Error,
    onError = Color.White
)

private val DeepNightLightColorScheme = lightColorScheme(
    primary = DeepNightColors.Primary,
    onPrimary = Color.White,
    secondary = DeepNightColors.Secondary,
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1C1C),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF666666),
    error = DeepNightColors.Error,
    onError = Color.White
)

// Mint Morning Theme
private val MintMorningDarkColorScheme = darkColorScheme(
    primary = MintMorningColors.Primary,
    onPrimary = MintMorningColors.OnPrimary,
    secondary = MintMorningColors.Secondary,
    onSecondary = MintMorningColors.OnSecondary,
    background = Color(0xFF1A2A28),
    onBackground = MintMorningColors.OnBackground,
    surface = Color(0xFF243635),
    onSurface = MintMorningColors.OnSurface,
    surfaceVariant = Color(0xFF2E403D),
    onSurfaceVariant = MintMorningColors.OnSurfaceVariant,
    error = MintMorningColors.Error,
    onError = Color.White
)

private val MintMorningLightColorScheme = lightColorScheme(
    primary = MintMorningColors.Primary,
    onPrimary = MintMorningColors.OnPrimary,
    secondary = MintMorningColors.Secondary,
    onSecondary = MintMorningColors.OnSecondary,
    background = MintMorningColors.Background,
    onBackground = MintMorningColors.OnBackground,
    surface = MintMorningColors.Surface,
    onSurface = MintMorningColors.OnSurface,
    surfaceVariant = MintMorningColors.SurfaceVariant,
    onSurfaceVariant = MintMorningColors.OnSurfaceVariant,
    error = MintMorningColors.Error,
    onError = Color.White
)

// Scholar Blue Theme
private val ScholarBlueDarkColorScheme = darkColorScheme(
    primary = ScholarBlueColors.Primary,
    onPrimary = ScholarBlueColors.OnPrimary,
    secondary = ScholarBlueColors.Secondary,
    onSecondary = ScholarBlueColors.OnSecondary,
    background = Color(0xFF1A1F2E),
    onBackground = ScholarBlueColors.OnBackground,
    surface = Color(0xFF252B3D),
    onSurface = ScholarBlueColors.OnSurface,
    surfaceVariant = Color(0xFF2E3649),
    onSurfaceVariant = ScholarBlueColors.OnSurfaceVariant,
    error = ScholarBlueColors.Error,
    onError = Color.White
)

private val ScholarBlueLightColorScheme = lightColorScheme(
    primary = ScholarBlueColors.Primary,
    onPrimary = ScholarBlueColors.OnPrimary,
    secondary = ScholarBlueColors.Secondary,
    onSecondary = ScholarBlueColors.OnSecondary,
    background = ScholarBlueColors.Background,
    onBackground = ScholarBlueColors.OnBackground,
    surface = ScholarBlueColors.Surface,
    onSurface = ScholarBlueColors.OnSurface,
    surfaceVariant = ScholarBlueColors.SurfaceVariant,
    onSurfaceVariant = ScholarBlueColors.OnSurfaceVariant,
    error = ScholarBlueColors.Error,
    onError = Color.White
)

/**
 * Main theme composable for the Sparkle Note application.
 * Supports multiple theme styles and automatic dark/light switching.
 */
@Composable
fun SparkleNoteTheme(
    themeStyle: ThemeStyle = ThemeStyle.NORDIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeStyle) {
        ThemeStyle.NORDIC -> {
            if (darkTheme) NordicDarkColorScheme else NordicLightColorScheme
        }
        ThemeStyle.DEEP_NIGHT -> {
            if (darkTheme) DeepNightDarkColorScheme else DeepNightLightColorScheme
        }
        ThemeStyle.MINT_MORNING -> {
            if (darkTheme) MintMorningDarkColorScheme else MintMorningLightColorScheme
        }
        ThemeStyle.SCHOLAR_BLUE -> {
            if (darkTheme) ScholarBlueDarkColorScheme else ScholarBlueLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}