package com.sparkle.note.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sparkle.note.ui.components.InspirationCard
import com.sparkle.note.ui.components.QuickRecordSection

/**
 * Main screen of the Sparkle Note application.
 * Displays quick record section and list of inspirations with real data integration.
 * Uses Hilt dependency injection for production-ready architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is MainEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is MainEvent.ShowDeleteSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                    // TODO: Add undo action to Snackbar
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Sparkle Note",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick record section
            QuickRecordSection(
                content = uiState.currentContent,
                onContentChange = viewModel::onContentChange,
                selectedTheme = uiState.selectedTheme,
                onThemeSelect = viewModel::onThemeSelect,
                onSave = viewModel::onSaveInspiration
            )
            
            // Theme selector chips
            if (uiState.themes.isNotEmpty()) {
                ThemeSelector(
                    themes = uiState.themes,
                    selectedTheme = uiState.selectedTheme,
                    onThemeSelect = viewModel::onThemeSelect,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Inspirations list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.inspirations) { inspiration ->
                    InspirationCard(
                        content = inspiration.content,
                        themeName = inspiration.themeName,
                        createdAtText = formatTimeAgo(inspiration.createdAt),
                        onClick = { /* Handle card click */ },
                        onDelete = { viewModel.onDeleteInspiration(inspiration.id) }
                    )
                }
            }
            
            // Empty state
            if (uiState.inspirations.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            }
        }
    }
}

/**
 * Theme selector with chip-style buttons.
 */
@Composable
fun ThemeSelector(
    themes: List<String>,
    selectedTheme: String,
    onThemeSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        themes.forEach { theme ->
            FilterChip(
                selected = theme == selectedTheme,
                onClick = { onThemeSelect(theme) },
                label = { Text(theme) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * Empty state when no inspirations exist.
 */
@Composable
fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "💡",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "还没有灵感记录",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "开始记录你的第一个灵感吧！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Formats timestamp to relative time string.
 */
private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3_600_000 -> "${diff / 60_000}分钟前"
        diff < 86_400_000 -> "${diff / 3_600_000}小时前"
        else -> "${diff / 86_400_000}天前"
    }
}