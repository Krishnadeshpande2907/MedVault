package com.medvault.di

import com.medvault.data.repository.ContactRepository
import com.medvault.data.repository.MediaRepository
import com.medvault.data.repository.VisitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repositories.
 * Repositories are provided as singletons — they coordinate DAO access
 * and file I/O and must be shared across ViewModels.
 *
 * Note: Actual @Provides methods are defined in each repository's companion
 * or via constructor injection (@Inject constructor). This module exists
 * to document the binding intent. Repositories use @Inject constructor directly.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Repositories use @Inject constructor — Hilt auto-binds them.
    // This module can be used for interface bindings if we refactor to interfaces later.
}
