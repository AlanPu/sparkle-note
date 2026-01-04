package com.sparkle.note.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced inspiration card with animations, swipe gestures, and long press support.
 * Provides modern Material Design 3 interactions and visual feedback.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EnhancedInspirationCard(
    content: String,
    themeName: String,
    createdAtText: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val displayContent = if (content.length > 100) {
        content.take(97) + "..."
    } else {
        content
    }
    
    val themeEmoji = getThemeEmoji(themeName)
    
    // Animation states
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    val elevation = remember { Animatable(1f) }
    
    // Selection animation
    LaunchedEffect(isSelected) {
        if (isSelected) {
            scale.animateTo(
                targetValue = 1.02f,
                animationSpec = tween(durationMillis = 200)
            )
            elevation.animateTo(
                targetValue = 3f,
                animationSpec = tween(durationMillis = 200)
            )
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200)
            )
            elevation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200)
            )
        }
    }
    
    // Card colors based on selection state
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale.value)
            .alpha(alpha.value),
        colors = cardColors,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.value.dp
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Content preview with fade-in animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)) + slideInVertically(
                        animationSpec = tween(durationMillis = 300)
                    ),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Text(
                        text = displayContent,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bottom info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Theme with emoji
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = themeEmoji,
                            style = TextStyle(fontSize = 14.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = themeName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Time with icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = createdAtText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Delete button (appears on hover/long press)
            if (isSelected) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Helper function to get theme emoji.
 */
private fun getThemeEmoji(themeName: String): String {
    return when (themeName) {
        "产品设计" -> "💡"
        "技术开发" -> "⚙️"
        "生活感悟" -> "🌟"
        "工作思考" -> "💼"
        "学习笔记" -> "📚"
        "创意想法" -> "🎨"
        else -> "💭"
    }
}