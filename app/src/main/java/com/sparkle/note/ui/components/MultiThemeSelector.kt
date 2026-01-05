package com.sparkle.note.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Multi-theme selector component for filtering inspirations by multiple themes.
 * Allows users to select multiple themes simultaneously.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiThemeSelector(
    themes: List<String>,
    selectedThemes: List<String>,
    onThemeToggle: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "筛选主题 (${selectedThemes.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (selectedThemes.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("清除全部")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(themes) { theme ->
                FilterChip(
                    selected = selectedThemes.contains(theme),
                    onClick = { onThemeToggle(theme) },
                    label = {
                        Text(
                            text = theme,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = if (selectedThemes.contains(theme)) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

/**
 * Compact multi-theme selector for bottom input area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactMultiThemeSelector(
    themes: List<String>,
    selectedThemes: List<String>,
    onThemeToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "主题筛选: ${selectedThemes.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(themes) { theme ->
                FilterChip(
                    selected = selectedThemes.contains(theme),
                    onClick = { onThemeToggle(theme) },
                    label = {
                        Text(
                            text = theme,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}