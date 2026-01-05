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
 * Component for quick recording of inspiration notes.
 * Provides a text input field with theme selection and save functionality.
 */
@Composable
fun QuickRecordSection(
    content: String,
    onContentChange: (String) -> Unit,
    selectedTheme: String,
    onThemeSelect: (String) -> Unit,
    onSave: () -> Unit,
    themes: List<String> = emptyList(), // 动态主题列表
    modifier: Modifier = Modifier
) {
    // Animation states
    val scale = remember { Animatable(0.95f) }
    val alpha = remember { Animatable(0.8f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .scale(scale.value)
            .alpha(alpha.value),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A) // Nordic surface dark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "快速记录",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE5E5E5) // Nordic text primary dark
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content input field
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text(
                        text = "记录你的灵感...",
                        color = Color(0xFF888888), // Nordic text secondary dark
                        style = TextStyle(fontSize = 14.sp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE5E5E5),
                    unfocusedTextColor = Color(0xFFE5E5E5),
                    focusedBorderColor = Color(0xFF4A90E2), // Nordic blue
                    unfocusedBorderColor = Color(0xFF333333), // Nordic divider dark
                    focusedContainerColor = Color(0xFF1A1A1A), // Nordic background dark
                    unfocusedContainerColor = Color(0xFF1A1A1A)
                ),
                textStyle = TextStyle(fontSize = 14.sp),
                maxLines = 6
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Theme selector and save button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                // Theme selector dropdown
                ThemeSelector(
                    selectedTheme = selectedTheme,
                    onThemeSelect = onThemeSelect,
                    themes = themes // 传递动态主题列表
                )
                
                // Character count
                Text(
                    text = "${content.length}/500",
                    color = Color(0xFF888888), // Nordic text secondary dark
                    style = TextStyle(fontSize = 12.sp),
                    textAlign = TextAlign.End
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Save button
            Button(
                onClick = onSave,
                enabled = content.isNotBlank() && content.length <= 500,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2), // Nordic blue
                    disabledContainerColor = Color(0xFF333333) // Nordic divider dark
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "保存",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * Theme selector dropdown component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelector(
    selectedTheme: String,
    onThemeSelect: (String) -> Unit,
    themes: List<String> = emptyList(), // 动态主题列表
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedTheme,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .width(120.dp),
            textStyle = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A90E2) // Nordic blue
            ),
            trailingIcon = { 
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4A90E2),
                unfocusedBorderColor = Color(0xFF333333),
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A)
            ),
            singleLine = true
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // 使用动态主题列表，如果没有提供则显示默认主题
            val displayThemes = if (themes.isEmpty()) {
                listOf("未分类", "产品设计", "技术开发", "生活感悟", "读书笔记", "工作思考")
            } else {
                themes
            }
            
            displayThemes.forEach { theme ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = theme,
                            style = TextStyle(fontSize = 14.sp)
                        )
                    },
                    onClick = {
                        onThemeSelect(theme)
                        expanded = false
                    }
                )
            }
        }
    }
}