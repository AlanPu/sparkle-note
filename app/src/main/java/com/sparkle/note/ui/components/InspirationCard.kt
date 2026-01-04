package com.sparkle.note.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha

/**
 * Card component for displaying an inspiration note.
 * Shows content preview, theme, and creation time in Nordic design style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationCard(
    content: String,
    themeName: String,
    createdAtText: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayContent = if (content.length > 100) {
        content.take(97) + "..."
    } else {
        content
    }
    
    val themeEmoji = getThemeEmoji(themeName)
    
    // Animation states
    val scale = remember { Animatable(0.95f) }
    val alpha = remember { Animatable(0.8f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200)
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(scale.value)
            .alpha(alpha.value),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A) // Nordic surface dark
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Content preview
            Text(
                text = displayContent,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFFE5E5E5), // Nordic text primary dark
                    lineHeight = 20.sp
                ),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                // Theme with emoji
                Row {
                    Text(
                        text = themeEmoji,
                        style = TextStyle(fontSize = 14.sp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = themeName,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A90E2) // Nordic blue
                        )
                    )
                }
                
                // Time
                Text(
                    text = createdAtText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF888888) // Nordic text secondary dark
                    ),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * Returns appropriate emoji for different theme names.
 * @param themeName The theme name to get emoji for
 * @return Emoji character representing the theme
 */
private fun getThemeEmoji(themeName: String): String {
    return when (themeName.lowercase()) {
        "产品设计" -> "💡"
        "技术开发" -> "⚙️"
        "生活感悟" -> "🌟"
        "工作思考" -> "💼"
        "学习笔记" -> "📚"
        "创意想法" -> "🎨"
        else -> "💭"
    }
}