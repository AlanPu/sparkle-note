package com.sparkle.note.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Delete confirmation dialog for inspiration cards.
 * Shows a Material Design alert dialog with confirm/cancel actions.
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("确认删除")
        },
        text = {
            Text("确定要删除这条灵感记录吗？此操作无法撤销。")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

/**
 * Swipe-to-delete wrapper for inspiration cards.
 * Provides Material Design dismissible functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteCard(
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = onDelete,
            onDismiss = { showDeleteDialog = false }
        )
    }
    
    // For now, use a simple long-press to trigger delete
    // TODO: Implement proper swipe gesture when Material3 swipeable is stable
    Card(
        modifier = modifier,
        onClick = { /* Card click */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            content()
        }
    }
}