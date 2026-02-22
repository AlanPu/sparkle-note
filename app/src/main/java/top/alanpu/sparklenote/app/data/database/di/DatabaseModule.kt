package top.alanpu.sparklenote.app.data.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import top.alanpu.sparklenote.app.data.database.InspirationDatabase
import top.alanpu.sparklenote.app.data.database.dao.InspirationDao
import top.alanpu.sparklenote.app.data.database.dao.ThemeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for database components.
 * Provides Room database and DAO instances using Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the Room database instance.
     * Uses singleton pattern to ensure single database instance.
     * Includes migration from version 1 to 2 for independent theme management.
     */
    @Provides
    @Singleton
    fun provideInspirationDatabase(@ApplicationContext context: Context): InspirationDatabase {
        return Room.databaseBuilder(
            context,
            InspirationDatabase::class.java,
            "inspiration_database"
        )
        .addMigrations(top.alanpu.sparklenote.app.data.database.migration.MIGRATION_1_2)
        .build()
    }
    
    /**
     * Provides the InspirationDao instance.
     * DAO is used for all database operations.
     */
    @Provides
    @Singleton
    fun provideInspirationDao(database: InspirationDatabase): InspirationDao {
        return database.inspirationDao()
    }
    
    /**
     * Provides the ThemeDao instance.
     * DAO is used for theme management operations.
     */
    @Provides
    @Singleton
    fun provideThemeDao(database: InspirationDatabase): ThemeDao {
        return database.themeDao()
    }
}