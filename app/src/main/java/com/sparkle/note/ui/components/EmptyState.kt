package com.sparkle.note.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Empty state component shown when there are no inspirations.
 * Provides a friendly message and quick action to create the first inspiration.
 */
@Composable
fun EmptyState(
    onCreateFirstInspiration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Light bulb icon (simplified version)
        Text(
            text = "💡",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "还没有灵感",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "开始记录你的第一个想法吧",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(
            onClick = onCreateFirstInspiration,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(text = "创建第一个灵感")
        }
    }
}

/**
 * Preview for EmptyState component.
 */
@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    MaterialTheme {
        EmptyState(
            onCreateFirstInspiration = {}
        )
    }
}
