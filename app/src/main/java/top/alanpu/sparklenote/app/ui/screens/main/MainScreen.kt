package top.alanpu.sparklenote.app.ui.screens.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.alanpu.sparklenote.app.ui.components.InspirationCard
import top.alanpu.sparklenote.app.ui.components.QuickRecordSection
import top.alanpu.sparklenote.app.ui.components.InspirationCardLongPressMenu

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
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // 长按菜单状态
    var selectedInspiration by remember { mutableStateOf<top.alanpu.sparklenote.app.domain.model.Inspiration?>(null) }
    var showLongPressMenu by remember { mutableStateOf(false) }
    
    // 长按处理函数
    val handleLongPress: (top.alanpu.sparklenote.app.domain.model.Inspiration) -> Unit = { inspiration ->
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
                is MainEvent.CopyToClipboard -> {
                    // 复制到剪贴板
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("笔记内容", event.content)
                    clipboard.setPrimaryClip(clip)
                    snackbarHostState.showSnackbar("内容已复制到剪贴板")
                }
                is MainEvent.OpenLink -> {
                    // 打开链接
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(event.url))
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("无法打开链接: ${e.message}")
                    }
                }
            }
        }
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
                onSave = viewModel::onSaveInspiration,
                themes = uiState.themes // 传递动态主题列表
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
                        onLongClick = { handleLongPress(inspiration) }, // 添加长按支持
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