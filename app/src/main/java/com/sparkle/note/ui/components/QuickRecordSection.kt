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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                // Theme selector (simplified)
                Text(
                    text = selectedTheme,
                    color = Color(0xFF4A90E2), // Nordic blue
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
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