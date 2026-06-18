package com.money.manager.ex.data.repository

import com.money.manager.ex.data.local.DatabaseManager
import com.money.manager.ex.data.mapper.toDomain
import com.money.manager.ex.domain.model.Transaction
import com.money.manager.ex.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val databaseManager: DatabaseManager
) : TransactionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getRecentTransactions(limit: Int, accountId: Int?): Flow<List<Transaction>> {
        return databaseManager.database.flatMapLatest { db ->
            if (db == null || accountId == null) return@flatMapLatest flowOf(emptyList())

            db.transactionDao().getRecentTransactions(limit, accountId)
                .map { entities -> entities.map { it.toDomain() } }
        }
    }
}
