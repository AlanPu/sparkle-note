package top.alanpu.sparklenote.app.data.database.di

import android.app.Application
import android.content.Context
import top.alanpu.sparklenote.app.data.repository.InspirationRepositoryImpl
import top.alanpu.sparklenote.app.data.repository.ThemeRepositoryImpl
import top.alanpu.sparklenote.app.domain.repository.InspirationRepository
import top.alanpu.sparklenote.app.domain.repository.ThemeRepository
import top.alanpu.sparklenote.app.utils.BackupManager
import top.alanpu.sparklenote.app.ui.theme.ThemeManager
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