package com.money.manager.ex.data.repository

import com.money.manager.ex.domain.model.Transaction
import com.money.manager.ex.domain.model.TransactionCode
import com.money.manager.ex.domain.model.TransactionStatus
import com.money.manager.ex.domain.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class FakeTransactionRepositoryImpl @Inject constructor() : TransactionRepository {
    override fun getRecentTransactions(limit: Int, accountId: Int?): Flow<List<Transaction>> = flow {
        delay(300)
        
        val transactions = mutableListOf<Transaction>()
        
        // Account ID 1
        transactions.addAll(listOf(
            Transaction(1, 1, null, 1, TransactionCode.WITHDRAWAL, -89.99, TransactionStatus.NORMAL, null, "Acc#1: Nota Trans. 1", 1, LocalDate.now(), null, "Amazon", "Shopping"),
            Transaction(2, 1, null, 2, TransactionCode.DEPOSIT, 3200.0, TransactionStatus.RECONCILED, null, "Acc#1: Nota Trans. 2", 2, LocalDate.now().minusDays(1), null, "Salary", "Salary"),
            Transaction(3, 1, null, 3, TransactionCode.WITHDRAWAL, -1200.0, TransactionStatus.NORMAL, null, "Acc#1: Nota Trans. 3", 3, LocalDate.now().minusDays(2), null, "Landlord", "Rent"),
            Transaction(4, 1, 2, 0, TransactionCode.TRANSFER, 500.0, TransactionStatus.NORMAL, null, "Acc#1: Nota Trans. 4", 4, LocalDate.now().minusDays(3), 500.0, "Internal Transfer", "Transfer"),
            Transaction(5, 1, null, 4, TransactionCode.WITHDRAWAL, -54.20, TransactionStatus.NORMAL, null, "Acc#1: Nota Trans. 5", 5, LocalDate.now().minusDays(3), null, "Supermarket", "Food")
        ))

        // Account ID 2 (Random transactions starting from ID 10)
        for (i in 0 until 5) {
            val id = 10 + i
            transactions.add(
                Transaction(
                    id = id,
                    accountId = 2,
                    payeeId = i + 5,
                    transCode = if (i % 2 == 0) TransactionCode.WITHDRAWAL else TransactionCode.DEPOSIT,
                    transAmount = if (i % 2 == 0) -20.0 * (i + 1) else 150.0 * (i + 1),
                    notes = "Acc#2: Nota Trans. $id",
                    transDate = LocalDate.now().minusDays(i.toLong()),
                    payee = "Payee $id",
                    category = "Category $id"
                )
            )
        }

        // Account ID 3 (Random transactions starting from ID 20)
        for (i in 0 until 5) {
            val id = 20 + i
            transactions.add(
                Transaction(
                    id = id,
                    accountId = 3,
                    payeeId = i + 10,
                    transCode = if (i % 2 == 0) TransactionCode.WITHDRAWAL else TransactionCode.DEPOSIT,
                    transAmount = if (i % 2 == 0) -15.0 * (i + 1) else 100.0 * (i + 1),
                    notes = "Acc#3: Nota Trans. $id",
                    transDate = LocalDate.now().minusDays(i.toLong()),
                    payee = "Payee $id",
                    category = "Category $id"
                )
            )
        }

        val filtered = if (accountId != null) {
            transactions.filter { it.accountId == accountId || it.toAccountId == accountId }
        } else {
            transactions
        }

        emit(filtered.take(limit))
    }
}
