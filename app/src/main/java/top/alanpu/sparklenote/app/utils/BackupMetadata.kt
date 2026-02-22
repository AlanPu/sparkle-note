package top.alanpu.sparklenote.app.utils

import kotlinx.serialization.Serializable
import java.util.Date

/**
 * Metadata for a backup file.
 * Contains information about the backup without the actual data.
 */
@Serializable
data class BackupMetadata(
    val id: String,
    val name: String,
    val createdAt: Long, // 使用Long而不是Date，避免序列化问题
    val size: Long,
    val version: String,
    val inspirationCount: Int,
    val description: String? = null
)