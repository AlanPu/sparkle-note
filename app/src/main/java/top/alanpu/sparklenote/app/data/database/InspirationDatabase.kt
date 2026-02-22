package top.alanpu.sparklenote.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import top.alanpu.sparklenote.app.data.database.dao.InspirationDao
import top.alanpu.sparklenote.app.data.database.dao.ThemeDao
import top.alanpu.sparklenote.app.data.entity.InspirationEntity
import top.alanpu.sparklenote.app.data.entity.ThemeEntity

/**
 * Room database for Sparkle Note application.
 * Now supports independent theme management with foreign key relationships.
 */
@Database(
    entities = [InspirationEntity::class, ThemeEntity::class],
    version = 2,
    exportSchema = false
)
abstract class InspirationDatabase : RoomDatabase() {
    
    abstract fun inspirationDao(): InspirationDao
    
    abstract fun themeDao(): ThemeDao
    
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