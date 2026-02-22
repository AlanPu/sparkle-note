package top.alanpu.sparklenote.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey

/**
 * Entity representing an inspiration note in the database.
 * Now uses foreign key relationship with ThemeEntity for better data integrity.
 */
@Entity(
    tableName = "inspirations",
    indices = [
        Index(value = ["content"]),
        Index(value = ["theme_name"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ThemeEntity::class,
            parentColumns = ["name"],
            childColumns = ["theme_name"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class InspirationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val content: String,
    
    val theme_name: String,
    
    val created_at: Long = System.currentTimeMillis(),
    
    val word_count: Int
)