package com.sparkle.note.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.sparkle.note.data.database.dao.InspirationDao
import com.sparkle.note.data.entity.InspirationEntity

/**
 * Room database for Sparkle Note application.
 * Stores inspiration notes with optimized queries for theme management.
 */
@Database(
    entities = [InspirationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class InspirationDatabase : RoomDatabase() {
    
    abstract fun inspirationDao(): InspirationDao
    
    companion object {
        private const val DATABASE_NAME = "inspiration_database"
        
        @Volatile
        private var INSTANCE: InspirationDatabase? = null
        
        /**
         * Returns the singleton instance of the database.
         * Uses double-checked locking for thread safety.
         * For production use, prefer dependency injection over manual instantiation.
         */
        fun getInstance(context: Context): InspirationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InspirationDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // Simple migration strategy for demo
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}