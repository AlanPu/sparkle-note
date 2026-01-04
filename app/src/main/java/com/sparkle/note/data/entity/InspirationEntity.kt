package com.sparkle.note.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity representing an inspiration note in the database.
 * Uses flattened design where theme is stored as string directly.
 */
@Entity(
    tableName = "inspirations",
    indices = [
        Index(value = ["content"]),
        Index(value = ["theme_name"])
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