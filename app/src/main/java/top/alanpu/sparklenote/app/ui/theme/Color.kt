package top.alanpu.sparklenote.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Nordic color palette following the design specification.
 * Based on Scandinavian design principles with dark theme as default.
 */

// Primary colors
val NordicBlue = Color(0xFF4A90E2)
val NordicGreen = Color(0xFF50C878)

// Dark theme colors
val NordicBackgroundDark = Color(0xFF1A1A1A)
val NordicSurfaceDark = Color(0xFF2A2A2A)
val NordicTextPrimaryDark = Color(0xFFE5E5E5)
val NordicTextSecondaryDark = Color(0xFF888888)
val NordicDividerDark = Color(0xFF333333)

// Light theme colors (for reference, app uses dark theme by default)
val NordicBackgroundLight = Color(0xFFF8F9FA)
val NordicSurfaceLight = Color(0xFFFFFFFF)
val NordicTextPrimaryLight = Color(0xFF2C3E50)
val NordicTextSecondaryLight = Color(0xFF7F8C8D)
val NordicDividerLight = Color(0xFFE9ECEF)

// Semantic colors
val NordicSuccess = Color(0xFF50C878)
val NordicWarning = Color(0xFFF39C12)
val NordicError = Color(0xFFE74C3C)
val NordicInfo = Color(0xFF4A90E2)

/**
 * Dark Mode - Deep Night Theme
 * Pure dark theme with neon blue-green accents
 */
object DeepNightColors {
    val Primary = Color(0xFF00D4FF)      // Neon Blue
    val OnPrimary = Color(0xFF000000)   // Black
    val Secondary = Color(0xFF00FF88)    // Neon Green  
    val OnSecondary = Color(0xFF000000) // Black
    val Background = Color(0xFF0D0D0D)   // Pure Black
    val OnBackground = Color(0xFFFFFFFF) // White
    val Surface = Color(0xFF1A1A1A)     // Dark Gray
    val OnSurface = Color(0xFFFFFFFF)   // White
    val SurfaceVariant = Color(0xFF2A2A2A) // Lighter Gray
    val OnSurfaceVariant = Color(0xFFB3B3B3) // Light Gray
    val Success = Color(0xFF00FF88)      // Neon Green
    val Warning = Color(0xFFFFD700)      // Gold
    val Error = Color(0xFFFF4444)        // Bright Red
    val Info = Color(0xFF00D4FF)        // Neon Blue
}

/**
 * Fresh Mode - Mint Morning Theme  
 * Light theme with soft mint green tones
 */
object MintMorningColors {
    val Primary = Color(0xFF4ECDC4)      // Mint Green
    val OnPrimary = Color(0xFFFFFFFF)   // White
    val Secondary = Color(0xFF44A08D)   // Forest Green
    val OnSecondary = Color(0xFFFFFFFF) // White
    val Background = Color(0xFFF8FFFE)  // Very Light Mint
    val OnBackground = Color(0xFF2C3E50) // Dark Blue-Gray
    val Surface = Color(0xFFFFFFFF)     // Pure White
    val OnSurface = Color(0xFF2C3E50)   // Dark Blue-Gray
    val SurfaceVariant = Color(0xFFE8F5F3) // Light Mint
    val OnSurfaceVariant = Color(0xFF7F8C8D) // Neutral Gray
    val Success = Color(0xFF44A08D)     // Forest Green
    val Warning = Color(0xFFF39C12)     // Orange
    val Error = Color(0xFFE74C3C)       // Red
    val Info = Color(0xFF4ECDC4)         // Mint Green
}

/**
 * Academic Mode - Scholar Blue Theme
 * Classic academic style with deep blue accents
 */
object ScholarBlueColors {
    val Primary = Color(0xFF2C5F8D)      // Academic Blue
    val OnPrimary = Color(0xFFFFFFFF)   // White
    val Secondary = Color(0xFF8B4513)  // Leather Brown
    val OnSecondary = Color(0xFFFFFFFF) // White
    val Background = Color(0xFFFAFBFC)  // Light Gray Background
    val OnBackground = Color(0xFF2C3E50) // Academic Black
    val Surface = Color(0xFFFFFFFF)     // Pure White
    val OnSurface = Color(0xFF2C3E50)   // Academic Black
    val SurfaceVariant = Color(0xFFF5F7FA) // Light Blue-Gray
    val OnSurfaceVariant = Color(0xFF5D6D7E) // Graphite Gray
    val Success = Color(0xFF27AE60)     // Success Green
    val Warning = Color(0xFFF39C12)     // Warning Orange
    val Error = Color(0xFFE74C3C)        // Error Red
    val Info = Color(0xFF2C5F8D)         // Academic Blue
}