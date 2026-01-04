package com.sparkle.note.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sparkle.note.ui.components.*

/**
 * Enhanced main screen with search, filter, and export functionality.
 * Integrates all advanced features into the main interface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMainScreen(
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
            
            // Action bar with search and export
            ActionBar(
                searchQuery = uiState.searchKeyword,
                onSearchQueryChange = viewModel::onSearch,
                onClearSearch = { viewModel.onSearch("") },
                onExportClick = { 
                    // TODO: Implement export functionality
                    viewModel.onExportInspirations()
                }
            )
            
            // Theme filter chips
            if (uiState.themes.isNotEmpty()) {
                ThemeFilterChips(
                    themes = uiState.themes,
                    selectedTheme = uiState.selectedFilterTheme,
                    onThemeFilter = viewModel::onThemeFilter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Inspirations list with enhanced cards
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.inspirations) { inspiration ->
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    
                    if (showDeleteDialog) {
                        DeleteConfirmationDialog(
                            onConfirm = { viewModel.onDeleteInspiration(inspiration.id) },
                            onDismiss = { showDeleteDialog = false }
                        )
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        onClick = { /* Handle card click */ }
                    ) {
                        InspirationCard(
                            content = inspiration.content,
                            themeName = inspiration.themeName,
                            createdAtText = formatTimeAgo(inspiration.createdAt),
                            onClick = { /* Handle card click */ },
                            onDelete = { showDeleteDialog = true }
                        )
                    }
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
 * Theme filter chips for filtering inspirations by theme.
 */
@Composable
fun ThemeFilterChips(
    themes: List<String>,
    selectedTheme: String?,
    onThemeFilter: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "按主题筛选",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All themes chip
            FilterChip(
                selected = selectedTheme == null,
                onClick = { onThemeFilter(null) },
                label = { Text("全部") }
            )
            
            // Individual theme chips
            themes.forEach { theme ->
                FilterChip(
                    selected = theme == selectedTheme,
                    onClick = { onThemeFilter(theme) },
                    label = { Text(theme) }
                )
            }
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