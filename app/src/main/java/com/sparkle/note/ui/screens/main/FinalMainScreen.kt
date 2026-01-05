package com.sparkle.note.ui.screens.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sparkle.note.ui.components.InspirationCardLongPressMenu
import com.sparkle.note.ui.components.CompactInspirationCard
import com.sparkle.note.ui.components.UltraCompactInspirationCard

/**
 * Final optimized main screen with bottom input and maximum card display.
 * Features:
 * - Bottom input area for better mobile UX
 * - Maximum space for inspiration cards
 * - Card density toggle
 * - Smooth animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalMainScreen(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var cardDensity by remember { mutableStateOf(CardDensity.NORMAL) }
    
    // 长按菜单状态
    var selectedInspiration by remember { mutableStateOf<com.sparkle.note.domain.model.Inspiration?>(null) }
    var showLongPressMenu by remember { mutableStateOf(false) }
    
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
                else -> {
                    // Handle other events
                }
            }
        }
    }
    
    // 长按处理函数
    val handleLongPress: (com.sparkle.note.domain.model.Inspiration) -> Unit = { inspiration ->
        selectedInspiration = inspiration
        showLongPressMenu = true
    }
    
    // 复制内容处理
    val handleCopyContent: () -> Unit = {
        selectedInspiration?.let { inspiration ->
            viewModel.copyInspirationContent(inspiration.content)
            showLongPressMenu = false
        }
    }
    
    // 打开链接处理
    val handleOpenLink: (String) -> Unit = { url ->
        viewModel.openLink(url)
        showLongPressMenu = false
    }
    
    // 显示长按菜单
    if (showLongPressMenu && selectedInspiration != null) {
        InspirationCardLongPressMenu(
            inspiration = selectedInspiration!!,
            onDismiss = { showLongPressMenu = false },
            onCopyContent = handleCopyContent,
            onOpenLink = handleOpenLink
        )
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
                    // Card density toggle
                    IconButton(onClick = {
                        cardDensity = when (cardDensity) {
                            CardDensity.NORMAL -> CardDensity.COMPACT
                            CardDensity.COMPACT -> CardDensity.ULTRA_COMPACT
                            CardDensity.ULTRA_COMPACT -> CardDensity.NORMAL
                        }
                    }) {
                        Icon(
                            imageVector = when (cardDensity) {
                                CardDensity.NORMAL -> Icons.Default.List
                                CardDensity.COMPACT -> Icons.Default.MoreVert
                                CardDensity.ULTRA_COMPACT -> Icons.Default.Menu
                            },
                            contentDescription = "卡片密度"
                        )
                    }
                    
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Bottom input section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Theme selector row
                    if (uiState.themes.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "选择主题：",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            ThemeSelector(
                                themes = uiState.themes,
                                selectedTheme = uiState.selectedTheme,
                                onThemeSelect = viewModel::onThemeSelect,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Input and save row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.Bottom
                    ) {
                        // Text input field - takes most space
                        OutlinedTextField(
                            value = uiState.currentContent,
                            onValueChange = viewModel::onContentChange,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 60.dp, max = 120.dp),
                            placeholder = {
                                Text(
                                    text = "记录你的灵感...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            maxLines = 4
                        )
                        
                        // Save button
                        FilledTonalButton(
                            onClick = viewModel::onSaveInspiration,
                            enabled = uiState.currentContent.isNotBlank() && uiState.currentContent.length <= 500,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "保存",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Character count
                    Text(
                        text = "${uiState.currentContent.length}/500",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.currentContent.length > 450) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Inspirations list - takes up most of the screen
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = when (cardDensity) {
                        CardDensity.NORMAL -> 8.dp
                        CardDensity.COMPACT -> 4.dp
                        CardDensity.ULTRA_COMPACT -> 2.dp
                    },
                    bottom = when (cardDensity) {
                        CardDensity.NORMAL -> 8.dp
                        CardDensity.COMPACT -> 4.dp
                        CardDensity.ULTRA_COMPACT -> 2.dp
                    },
                    start = when (cardDensity) {
                        CardDensity.NORMAL -> 12.dp
                        CardDensity.COMPACT -> 8.dp
                        CardDensity.ULTRA_COMPACT -> 4.dp
                    },
                    end = when (cardDensity) {
                        CardDensity.NORMAL -> 12.dp
                        CardDensity.COMPACT -> 8.dp
                        CardDensity.ULTRA_COMPACT -> 4.dp
                    }
                ),
                verticalArrangement = Arrangement.spacedBy(when (cardDensity) {
                    CardDensity.NORMAL -> 8.dp
                    CardDensity.COMPACT -> 4.dp
                    CardDensity.ULTRA_COMPACT -> 2.dp
                })
            ) {
                items(uiState.inspirations) { inspiration ->
                    when (cardDensity) {
                        CardDensity.NORMAL -> {
                            com.sparkle.note.ui.components.InspirationCard(
                                content = inspiration.content,
                                themeName = inspiration.themeName,
                                createdAtText = formatTimeAgo(inspiration.createdAt),
                                onClick = { /* Handle card click */ },
                                onLongClick = { handleLongPress(inspiration) }, // 添加长按支持
                                onDelete = { viewModel.onDeleteInspiration(inspiration.id) }
                            )
                        }
                        CardDensity.COMPACT -> {
                            CompactInspirationCard(
                                content = inspiration.content,
                                themeName = inspiration.themeName,
                                createdAtText = formatTimeAgo(inspiration.createdAt),
                                onClick = { /* Handle card click */ },
                                onLongClick = { handleLongPress(inspiration) }, // 添加长按支持
                                onDelete = { viewModel.onDeleteInspiration(inspiration.id) }
                            )
                        }
                        CardDensity.ULTRA_COMPACT -> {
                            UltraCompactInspirationCard(
                                content = inspiration.content,
                                themeName = inspiration.themeName,
                                createdAtText = formatTimeAgo(inspiration.createdAt),
                                onClick = { /* Handle card click */ },
                                onLongClick = { handleLongPress(inspiration) }, // 添加长按支持
                                onDelete = { viewModel.onDeleteInspiration(inspiration.id) }
                            )
                        }
                    }
                }
            }
            
            // Empty state with better spacing
            if (uiState.inspirations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    EmptyState()
                }
            }
        }
    }
}

/**
 * Card density options for display optimization.
 */
enum class CardDensity {
    NORMAL, COMPACT, ULTRA_COMPACT
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