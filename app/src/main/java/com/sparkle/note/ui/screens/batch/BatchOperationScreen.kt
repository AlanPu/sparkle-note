package com.sparkle.note.ui.screens.batch

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sparkle.note.domain.model.Inspiration

/**
 * Batch operation screen for multi-selection and batch operations.
 * Supports batch delete, export, and other operations on inspirations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchOperationScreen(
    onNavigateBack: () -> Unit,
    onInspirationClick: (Long) -> Unit,
    viewModel: BatchOperationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BatchOperationEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is BatchOperationEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is BatchOperationEvent.ExportComplete -> {
                    snackbarHostState.showSnackbar("导出完成: ${event.filePath}")
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.isSelectionMode) {
                            "已选择 ${uiState.selectedIds.size} 项"
                        } else {
                            "批量操作"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (uiState.isSelectionMode) {
                        // Select all button
                        IconButton(
                            onClick = { viewModel.selectAllItems() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "全选"
                            )
                        }
                        
                        // Cancel selection button
                        TextButton(
                            onClick = { viewModel.exitSelectionMode() }
                        ) {
                            Text("取消")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.isSelectionMode && uiState.selectedIds.isNotEmpty()) {
                BatchOperationBottomBar(
                    selectedCount = uiState.selectedIds.size,
                    onBatchDelete = { viewModel.performBatchDelete() },
                    onBatchExport = { viewModel.performBatchExport() }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.inspirations.isEmpty()) {
                EmptyBatchOperationView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.inspirations) { inspiration ->
                        BatchInspirationItem(
                            inspiration = inspiration,
                            isSelected = uiState.selectedIds.contains(inspiration.id),
                            isSelectionMode = uiState.isSelectionMode,
                            onClick = {
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleItemSelection(inspiration.id)
                                } else {
                                    onInspirationClick(inspiration.id)
                                }
                            },
                            onLongClick = {
                                if (!uiState.isSelectionMode) {
                                    viewModel.enterSelectionMode(inspiration.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BatchInspirationItem(
    inspiration: Inspiration,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            if (isSelectionMode) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = inspiration.themeName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = android.text.format.DateFormat.format("MM月dd日", inspiration.createdAt).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = inspiration.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                // Tags removed - Inspiration model doesn't have tags field
            }
        }
    }
}

@Composable
fun BatchOperationBottomBar(
    selectedCount: Int,
    onBatchDelete: () -> Unit,
    onBatchExport: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete button
            Button(
                onClick = onBatchDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("删除")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Export button
            Button(
                onClick = onBatchExport,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "导出",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("导出")
            }
        }
    }
}

@Composable
fun EmptyBatchOperationView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "暂无灵感记录",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "长按任意灵感项进入批量选择模式",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}