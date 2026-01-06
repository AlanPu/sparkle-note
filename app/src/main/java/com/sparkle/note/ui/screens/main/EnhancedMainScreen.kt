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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sparkle.note.ui.components.InspirationCardLongPressMenu
import androidx.navigation.compose.rememberNavController
import com.sparkle.note.ui.components.EnhancedThemeSelector
import com.sparkle.note.ui.components.MultiThemeSelector
import com.sparkle.note.R
import androidx.compose.ui.res.stringResource

/**
 * Final enhanced main screen with complete theme management integration.
 * Features:
 * - Bottom input area for better mobile UX
 * - Dynamic theme loading from ViewModel
 * - Quick theme creation without leaving main screen
 * - Quick theme management access
 * - Maximum space for inspiration cards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMainScreen(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
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
                is MainEvent.CopyToClipboard -> {
                    // 复制到剪贴板
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText(context.getString(R.string.clipboard_label_note_content), event.content)
                    clipboard.setPrimaryClip(clip)
                    snackbarHostState.showSnackbar(context.getString(R.string.message_copy_success))
                }
                is MainEvent.OpenLink -> {
                    // 打开链接
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(event.url))
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(context.getString(R.string.message_link_error, e.message))
                    }
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
                        text = stringResource(R.string.app_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("theme") }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.content_description_theme_management)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.content_description_advanced_search)
                        )
                    }
                    IconButton(onClick = { navController.navigate("batch") }) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.content_description_batch_operation)
                        )
                    }
                    IconButton(onClick = { navController.navigate("backup") }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.content_description_backup_management)
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
            // Bottom input section with complete theme management
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
                    // Theme selector and management row
                    if (uiState.themes.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.label_select_theme),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Enhanced theme selector (theme creation removed from main screen)
                            EnhancedThemeSelector(
                                selectedTheme = uiState.selectedTheme,
                                onThemeSelect = viewModel::onThemeSelect,
                                themes = uiState.themes,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Quick theme management access
                            IconButton(
                                onClick = { navController.navigate("theme") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.content_description_manage_themes),
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
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
                                    text = stringResource(R.string.hint_record_inspiration),
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
                                contentDescription = stringResource(R.string.content_description_save),
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
                    top = 8.dp,
                    bottom = 8.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.inspirations) { inspiration ->
                    com.sparkle.note.ui.components.InspirationCard(
                        content = inspiration.content,
                        themeName = inspiration.themeName,
                        createdAtText = formatTimeAgo(inspiration.createdAt, context),
                        onClick = { /* Handle card click */ },
                        onLongClick = { handleLongPress(inspiration) }, // 添加长按支持
                        onDelete = { viewModel.onDeleteInspiration(inspiration.id) }
                    )
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
 * Formats timestamp to relative time string.
 */
private fun formatTimeAgo(timestamp: Long, context: Context): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> context.getString(R.string.time_just_now)
        diff < 3_600_000 -> context.getString(R.string.time_minutes_ago, diff / 60_000)
        diff < 86_400_000 -> context.getString(R.string.time_hours_ago, diff / 3_600_000)
        else -> context.getString(R.string.time_days_ago, diff / 86_400_000)
    }
}