package com.sparkle.note.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sparkle.note.ui.theme.ThemeStyle

/**
 * Theme selection component for choosing app color schemes.
 * Provides a user-friendly interface to switch between different themes.
 */
@Composable
fun ThemeSelector(
    modifier: Modifier = Modifier,
    selectedTheme: ThemeStyle = ThemeStyle.NORDIC,
    onThemeSelected: (ThemeStyle) -> Unit = {}
) {
    var showThemeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = "主题风格",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = getThemeDescription(selectedTheme),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Button(
                onClick = { showThemeDialog = true },
                modifier = Modifier.height(36.dp)
            ) {
                Text("切换")
            }
        }
    }

    // Theme selection dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = selectedTheme,
            onThemeSelected = { themeStyle ->
                onThemeSelected(themeStyle)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

/**
 * Dialog for selecting theme style with preview cards
 */
@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeStyle,
    onThemeSelected: (ThemeStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择主题风格",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeStyle.values().forEach { themeStyle ->
                    ThemeOptionCard(
                        themeStyle = themeStyle,
                        isSelected = themeStyle == currentTheme,
                        onClick = { onThemeSelected(themeStyle) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * Individual theme option card with preview
 */
@Composable
private fun ThemeOptionCard(
    themeStyle: ThemeStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder(true)
        } else {
            null
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getThemeStyleName(themeStyle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getThemeDescription(themeStyle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Theme color preview
            ThemeColorPreview(themeStyle)
            
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Color preview for theme styles
 */
@Composable
private fun ThemeColorPreview(themeStyle: ThemeStyle) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val colors = when (themeStyle) {
            ThemeStyle.NORDIC -> listOf(
                Color(0xFF4A90E2), // Nordic Blue
                Color(0xFF50C878), // Nordic Green
                Color(0xFF2A2A2A)  // Dark Surface
            )
            ThemeStyle.DEEP_NIGHT -> listOf(
                Color(0xFF00D4FF), // Neon Blue
                Color(0xFF00FF88), // Neon Green
                Color(0xFF0D0D0D)  // Pure Black
            )
            ThemeStyle.MINT_MORNING -> listOf(
                Color(0xFF4ECDC4), // Mint Green
                Color(0xFF44A08D), // Forest Green
                Color(0xFFF8FFFE)  // Light Mint
            )
            ThemeStyle.SCHOLAR_BLUE -> listOf(
                Color(0xFF2C5F8D), // Academic Blue
                Color(0xFF8B4513), // Leather Brown
                Color(0xFFFAFBFC)  // Light Background
            )
        }
        
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, shape = RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Get theme style name for display
 */
private fun getThemeStyleName(themeStyle: ThemeStyle): String {
    return when (themeStyle) {
        ThemeStyle.NORDIC -> "北欧风格"
        ThemeStyle.DEEP_NIGHT -> "深邃夜空"
        ThemeStyle.MINT_MORNING -> "薄荷晨露"
        ThemeStyle.SCHOLAR_BLUE -> "学院蓝调"
    }
}

/**
 * Get theme description
 */
private fun getThemeDescription(themeStyle: ThemeStyle): String {
    return when (themeStyle) {
        ThemeStyle.NORDIC -> "经典的北欧简约设计"
        ThemeStyle.DEEP_NIGHT -> "纯黑背景配霓虹蓝绿点缀"
        ThemeStyle.MINT_MORNING -> "清新薄荷绿配柔和色调"
        ThemeStyle.SCHOLAR_BLUE -> "知性深蓝配经典学术风"
    }
}