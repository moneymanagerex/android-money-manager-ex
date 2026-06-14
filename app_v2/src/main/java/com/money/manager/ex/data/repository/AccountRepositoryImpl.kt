package com.money.manager.ex.data.repository

import com.money.manager.ex.data.local.DatabaseManager
import com.money.manager.ex.data.mapper.toDomain
import com.money.manager.ex.domain.model.Account
import com.money.manager.ex.domain.repository.AccountRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AccountRepositoryImpl @Inject constructor(
    private val databaseManager: DatabaseManager,
    private val fakeAccountRepository: FakeAccountRepositoryImpl
) : AccountRepository {
    
    override fun getOpenAccounts(): Flow<List<Account>> {
        return databaseManager.database.flatMapLatest { db ->
            if (db == null) {
                // Se non c'è un database reale, mostriamo i dati fake
                fakeAccountRepository.getOpenAccounts()
            } else {
                // Altrimenti, leggiamo i dati reali dal database Room
                db.accountDao().getOpenAccountsWithBalance().map { entities ->
                    entities.map { it.toDomain() }
                }
            }
        }
    }
}
