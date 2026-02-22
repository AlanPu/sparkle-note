package top.alanpu.sparklenote.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import top.alanpu.sparklenote.app.R

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
            text = stringResource(R.string.state_no_inspirations_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = stringResource(R.string.state_start_recording_hint),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(
            onClick = onCreateFirstInspiration,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(text = stringResource(R.string.action_create_first_inspiration))
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
