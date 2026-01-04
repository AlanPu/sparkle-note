package com.sparkle.note.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sparkle.note.ui.components.QuickRecordSection

/**
 * Enhanced main screen with navigation and all features integrated.
 * Provides access to all Day 3 advanced features through top bar actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMainScreen(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel()
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
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Sparkle Note",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("theme") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "主题管理"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "高级搜索"
                        )
                    }
                    IconButton(onClick = { navController.navigate("batch") }) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "批量操作"
                        )
                    }
                    IconButton(onClick = { navController.navigate("backup") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "备份管理"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                    com.sparkle.note.ui.components.InspirationCard(
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