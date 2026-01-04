package com.sparkle.note.utils

import com.sparkle.note.domain.model.Inspiration
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for exporting inspirations to various formats.
 * Handles markdown generation and file naming according to specifications.
 */
object ExportManager {
    
    private const val MAX_FILENAME_LENGTH = 100
    private const val CONTENT_PREVIEW_LENGTH = 10
    
    /**
     * Exports a single inspiration to markdown format.
     * Format: YAML front matter + content
     * 
     * @param inspiration The inspiration to export
     * @return Markdown formatted string
     */
    fun exportSingleToMarkdown(inspiration: Inspiration): String {
        return buildString {
            appendLine("---")
            appendLine("主题: ${inspiration.themeName}")
            appendLine("创建时间: ${formatDateTime(inspiration.createdAt)}")
            appendLine("字数: ${inspiration.wordCount}")
            appendLine("---")
            appendLine()
            appendLine(inspiration.content)
        }
    }
    
    /**
     * Exports multiple inspirations to markdown format.
     * Groups by theme and includes summary statistics.
     * 
     * @param inspirations List of inspirations to export
     * @return Markdown formatted string
     */
    fun exportBatchToMarkdown(inspirations: List<Inspiration>): String {
        if (inspirations.isEmpty()) {
            return "# 我的灵感笔记\n\n暂无灵感记录。"
        }
        
        return buildString {
            appendLine("# 我的灵感笔记")
            appendLine()
            appendLine("导出时间：${formatDateTime(System.currentTimeMillis())}")
            appendLine("灵感总数：${inspirations.size}条")
            appendLine("主题数量：${inspirations.distinctBy { it.themeName }.size}个")
            appendLine()
            
            // Group by theme
            val groupedByTheme = inspirations.groupBy { it.themeName }
            
            groupedByTheme.forEach { (theme, themeInspirations) ->
                appendLine("## $theme（${themeInspirations.size}条）")
                appendLine()
                
                themeInspirations.forEachIndexed { index, inspiration ->
                    appendLine("### ${index + 1}. ${inspiration.content.take(50)}...")
                    appendLine()
                    appendLine("**创建时间**: ${formatDateTime(inspiration.createdAt)}")
                    appendLine("**字数**: ${inspiration.wordCount}")
                    appendLine("**主题**: ${inspiration.themeName}")
                    appendLine()
                    appendLine(inspiration.content)
                    appendLine()
                    appendLine("---")
                    appendLine()
                }
            }
        }
    }
    
    /**
     * Generates filename for inspiration export.
     * Format: 主题名-内容前10字-时间戳.md
     * 
     * @param inspiration The inspiration to generate filename for
     * @return Filename string
     */
    fun generateFilename(inspiration: Inspiration): String {
        val theme = sanitizeFilename(inspiration.themeName)
        val contentPreview = sanitizeFilename(inspiration.content.take(CONTENT_PREVIEW_LENGTH))
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date(inspiration.createdAt))
        
        val filename = "${theme}-${contentPreview}-${timestamp}.md"
        
        // Ensure filename doesn't exceed maximum length
        return if (filename.length > MAX_FILENAME_LENGTH) {
            val truncatedPreview = sanitizeFilename(inspiration.content.take(CONTENT_PREVIEW_LENGTH / 2))
            "${theme}-${truncatedPreview}-${timestamp}.md"
        } else {
            filename
        }
    }
    
    /**
     * Sanitizes string for use in filenames.
     * Removes special characters and replaces spaces with hyphens.
     * 
     * @param input String to sanitize
     * @return Sanitized string
     */
    private fun sanitizeFilename(input: String): String {
        return input
            .replace(Regex("[^\\w\\s-]"), "") // Remove special characters
            .replace(Regex("\\s+"), "-") // Replace spaces with hyphens
            .trim()
    }
    
    /**
     * Formats timestamp to human-readable datetime string.
     * 
     * @param timestamp Timestamp in milliseconds
     * @return Formatted datetime string
     */
    private fun formatDateTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}