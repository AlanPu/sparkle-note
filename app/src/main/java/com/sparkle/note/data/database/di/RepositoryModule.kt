package com.sparkle.note.data.database.di

import android.app.Application
import android.content.Context
import com.sparkle.note.data.repository.InspirationRepositoryImpl
import com.sparkle.note.data.repository.ThemeRepositoryImpl
import com.sparkle.note.domain.repository.InspirationRepository
import com.sparkle.note.domain.repository.ThemeRepository
import com.sparkle.note.utils.BackupManager
import com.sparkle.note.ui.theme.ThemeManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for repository components.
 * Provides repository implementations using Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Binds the InspirationRepository implementation.
     * Makes InspirationRepositoryImpl available for dependency injection.
     */
    @Binds
    @Singleton
    abstract fun bindInspirationRepository(
        inspirationRepositoryImpl: InspirationRepositoryImpl
    ): InspirationRepository
    
    /**
     * Binds the ThemeRepository implementation.
     * Makes ThemeRepositoryImpl available for dependency injection.
     */
    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideContext(application: Application): Context = application
        
        @Provides
        @Singleton
        fun provideBackupManager(): BackupManager = BackupManager
        
        @Provides
        @Singleton
        fun provideThemeManager(application: Application): ThemeManager = ThemeManager(application)
    }
}