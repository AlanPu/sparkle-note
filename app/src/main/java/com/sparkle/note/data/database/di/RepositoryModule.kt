package com.sparkle.note.data.database.di

import com.sparkle.note.data.repository.InspirationRepositoryImpl
import com.sparkle.note.domain.repository.InspirationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
}