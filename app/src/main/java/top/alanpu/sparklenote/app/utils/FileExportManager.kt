package top.alanpu.sparklenote.app.utils

import android.content.Context
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * Utility class for handling file operations related to inspiration exports.
 * Provides functionality to save, share, and manage exported files.
 */
object FileExportManager {
    
    private const val EXPORT_DIR_NAME = "SparkleNoteExports"
    private const val AUTHORITY_SUFFIX = ".fileprovider"
    
    /**
     * Saves markdown content to a file in the app's external files directory.
     * 
     * @param context Android context
     * @param filename Name of the file to create
     * @param content Markdown content to save
     * @return Result containing the saved file path or error message
     */
    fun saveMarkdownFile(
        context: Context,
        filename: String,
        content: String
    ): Result<File> {
        return try {
            // Get the app's external files directory
            val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR_NAME)
            
            // Create directory if it doesn't exist
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            // Create the file
            val file = File(exportDir, filename)
            
            // Write content to file
            file.writeText(content)
            
            Result.success(file)
            
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SecurityException) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets the URI for a file using FileProvider for sharing.
     * 
     * @param context Android context
     * @param file The file to get URI for
     * @return URI that can be used for sharing
     */
    fun getFileUri(context: Context, file: File): Result<android.net.Uri> {
        return try {
            val authority = context.packageName + AUTHORITY_SUFFIX
            val uri = FileProvider.getUriForFile(context, authority, file)
            Result.success(uri)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets all exported files in the app's export directory.
     * 
     * @param context Android context
     * @return List of exported markdown files
     */
    fun getExportedFiles(context: Context): List<File> {
        val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR_NAME)
        
        return if (exportDir.exists() && exportDir.isDirectory) {
            exportDir.listFiles { file ->
                file.extension.equals("md", ignoreCase = true)
            }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * Deletes an exported file.
     * 
     * @param context Android context
     * @param filename Name of the file to delete
     * @return Result indicating success or failure
     */
    fun deleteExportedFile(context: Context, filename: String): Result<Unit> {
        return try {
            val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR_NAME)
            val file = File(exportDir, filename)
            
            if (file.exists()) {
                if (file.delete()) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Failed to delete file"))
                }
            } else {
                Result.failure(IOException("File does not exist"))
            }
        } catch (e: SecurityException) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets the size of the export directory.
     * 
     * @param context Android context
     * @return Size in bytes
     */
    fun getExportDirectorySize(context: Context): Long {
        val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR_NAME)
        
        return if (exportDir.exists() && exportDir.isDirectory) {
            exportDir.listFiles()?.sumOf { it.length() } ?: 0L
        } else {
            0L
        }
    }
    
    /**
     * Formats file size to human readable string.
     * 
     * @param size Size in bytes
     * @return Formatted string (e.g., "1.2 MB")
     */
    fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var currentSize = size.toDouble()
        var unitIndex = 0
        
        while (currentSize >= 1024 && unitIndex < units.size - 1) {
            currentSize /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", currentSize, units[unitIndex])
    }
}