package com.sparkle.note.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Enhanced theme selector with quick theme creation functionality.
 * Allows users to select existing themes or quickly create new ones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedThemeSelector(
    selectedTheme: String,
    onThemeSelect: (String) -> Unit,
    themes: List<String>,
    onCreateNewTheme: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newThemeName by remember { mutableStateOf("") }
    
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
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.labelMedium,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Existing themes
            themes.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(theme) },
                    onClick = {
                        onThemeSelect(theme)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
            
            HorizontalDivider()
            
            // Create new theme option
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("新建主题")
                    }
                },
                onClick = {
                    showCreateDialog = true
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
        }
    }
    
    // Create new theme dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { 
                showCreateDialog = false
                newThemeName = ""
            },
            title = { Text("创建新主题") },
            text = {
                Column {
                    Text(
                        text = "主题名称",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newThemeName,
                        onValueChange = { newThemeName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("输入主题名称") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newThemeName.isNotBlank()) {
                            onCreateNewTheme(newThemeName)
                            onThemeSelect(newThemeName)
                            showCreateDialog = false
                            newThemeName = ""
                        }
                    },
                    enabled = newThemeName.isNotBlank()
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCreateDialog = false
                    newThemeName = ""
                }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * Quick theme selector for bottom input area with theme creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickThemeSelector(
    selectedTheme: String,
    onThemeSelect: (String) -> Unit,
    themes: List<String>,
    onCreateNewTheme: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newThemeName by remember { mutableStateOf("") }
    
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
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.labelMedium,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Existing themes
            themes.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(theme) },
                    onClick = {
                        onThemeSelect(theme)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
            
            if (themes.isNotEmpty()) {
                HorizontalDivider()
            }
            
            // Create new theme option
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("新建主题", style = MaterialTheme.typography.labelMedium)
                    }
                },
                onClick = {
                    showCreateDialog = true
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
        }
    }
    
    // Compact create new theme dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { 
                showCreateDialog = false
                newThemeName = ""
            },
            title = { Text("新建主题", style = MaterialTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = newThemeName,
                    onValueChange = { newThemeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("主题名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newThemeName.isNotBlank()) {
                            onCreateNewTheme(newThemeName)
                            onThemeSelect(newThemeName)
                            showCreateDialog = false
                            newThemeName = ""
                        }
                    },
                    enabled = newThemeName.isNotBlank()
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCreateDialog = false
                    newThemeName = ""
                }) {
                    Text("取消")
                }
            }
        )
    }
}