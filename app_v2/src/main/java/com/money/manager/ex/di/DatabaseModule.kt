package com.money.manager.ex.di

import android.content.Context
import com.money.manager.ex.data.local.DatabaseManager
import com.money.manager.ex.data.local.dao.AccountDao
import com.money.manager.ex.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabaseManager(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository
    ): DatabaseManager {
        return DatabaseManager(context, settingsRepository)
    }

    @Provides
    fun provideAccountDao(databaseManager: DatabaseManager): AccountDao? {
        // This is tricky because AccountDao is needed by repositories, 
        // but it might not be available yet. 
        // We'll handle this in the repositories by observing databaseManager.database
        return null
    }
}
