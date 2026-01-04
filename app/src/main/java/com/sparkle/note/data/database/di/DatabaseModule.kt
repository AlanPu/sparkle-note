package com.sparkle.note.data.database.di

import android.content.Context
import androidx.room.Room
import com.sparkle.note.data.database.InspirationDatabase
import com.sparkle.note.data.database.dao.InspirationDao
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
     */
    @Provides
    @Singleton
    fun provideInspirationDatabase(@ApplicationContext context: Context): InspirationDatabase {
        return Room.databaseBuilder(
            context,
            InspirationDatabase::class.java,
            "inspiration_database"
        )
        .fallbackToDestructiveMigration() // Simple migration strategy for demo
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
}