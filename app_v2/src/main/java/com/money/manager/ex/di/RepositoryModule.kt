package com.money.manager.ex.di

import com.money.manager.ex.data.repository.*
import com.money.manager.ex.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    @Actual
    abstract fun bindPeriodSummaryRepositoryActual(
        impl: PeriodSummaryRepositoryActualImpl
    ): PeriodSummaryRepository

    @Binds
    @Singleton
    @Forecast
    abstract fun bindPeriodSummaryRepositoryForecast(
        impl: PeriodSummaryRepositoryForecastImpl
    ): PeriodSummaryRepository

    @Binds
    @Singleton
    abstract fun bindPeriodSummaryRepository(
        impl: PeriodSummaryRepositoryImpl
    ): PeriodSummaryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
