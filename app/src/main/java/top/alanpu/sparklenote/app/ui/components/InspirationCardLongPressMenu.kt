package top.alanpu.sparklenote.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.alanpu.sparklenote.app.domain.model.Inspiration
import top.alanpu.sparklenote.app.utils.LinkUtils

/**
 * Long press context menu for inspiration cards.
 * Provides quick actions like copy content and open links.
 */
@Composable
fun InspirationCardLongPressMenu(
    inspiration: Inspiration,
    onDismiss: () -> Unit,
    onCopyContent: () -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 提取链接（使用remember避免重复计算）
    val links = remember(inspiration.content) {
        LinkUtils.extractLinks(inspiration.content)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { 
            Text(
                text = "笔记操作",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 复制内容选项
                TextButton(
                    onClick = onCopyContent,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Share, // 使用可用的图标
                            contentDescription = "复制",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("复制内容")
                    }
                }
                
                // 如果有链接，显示打开链接选项
                if (links.isNotEmpty()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    Text(
                        text = "打开链接:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    links.forEach { link ->
                        TextButton(
                            onClick = { onOpenLink(link) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search, // 使用可用的图标
                                    contentDescription = "打开链接",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = LinkUtils.formatLinkForDisplay(link, 40),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * Simplified long press menu without links section.
 * Used when content has no links.
 */
@Composable
fun SimpleInspirationCardLongPressMenu(
    onCopyContent: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { 
            Text(
                text = "笔记操作",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            TextButton(
                onClick = onCopyContent,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            Icons.Default.Share, // 使用可用的图标
                            contentDescription = "复制",
                            modifier = Modifier.size(20.dp)
                        )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("复制内容")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}