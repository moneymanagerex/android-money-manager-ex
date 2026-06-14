package com.money.manager.ex.di

import com.money.manager.ex.data.repository.FakeAccountRepositoryImpl
import com.money.manager.ex.data.repository.FakePeriodSummaryRepositoryImpl
import com.money.manager.ex.data.repository.FakeTransactionRepositoryImpl
import com.money.manager.ex.domain.repository.AccountRepository
import com.money.manager.ex.domain.repository.PeriodSummaryRepository
import com.money.manager.ex.domain.repository.TransactionRepository
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
        fakeAccountRepositoryImpl: FakeAccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        fakeTransactionRepositoryImpl: FakeTransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindPeriodSummaryRepository(
        fakePeriodSummaryRepositoryImpl: FakePeriodSummaryRepositoryImpl
    ): PeriodSummaryRepository
}
