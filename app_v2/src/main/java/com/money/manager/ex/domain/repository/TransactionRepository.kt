package com.money.manager.ex.domain.repository

import com.money.manager.ex.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getRecentTransactions(limit: Int, accountId: Int?): Flow<List<Transaction>>
}
