package com.sparkle.note.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Compact inspiration card for maximum display efficiency.
 * Optimized for list display with minimal spacing.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CompactInspirationCard(
    content: String,
    themeName: String,
    createdAtText: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayContent = if (content.length > 120) {
        content.take(117) + "..."
    } else {
        content
    }
    
    val themeEmoji = getThemeEmoji(themeName)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Content preview
            Text(
                text = displayContent,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom info row - compact layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Theme info
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = themeEmoji,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = themeName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Time and actions
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // Time
                    Text(
                        text = createdAtText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "删除",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ultra compact inspiration card for maximum density.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UltraCompactInspirationCard(
    content: String,
    themeName: String,
    createdAtText: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayContent = if (content.length > 80) {
        content.take(77) + "..."
    } else {
        content
    }
    
    val themeEmoji = getThemeEmoji(themeName)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Content and theme
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = displayContent,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = themeEmoji,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                    Text(
                        text = "$themeName • $createdAtText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error
                )
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