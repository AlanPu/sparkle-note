package top.alanpu.sparklenote.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a theme in the database.
 * Themes are independent entities that can exist without inspirations.
 */
@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey
    val name: String,
    
    val icon: String = "💡",
    
    val color: Long = 0xFF4A90E2, // Default blue color
    
    val description: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val lastUsed: Long = System.currentTimeMillis(),
    
    val inspirationCount: Int = 0 // Cached count for performance
)