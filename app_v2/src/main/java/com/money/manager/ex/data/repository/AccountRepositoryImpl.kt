package com.money.manager.ex.data.repository

import com.money.manager.ex.data.local.dao.AccountDao
import com.money.manager.ex.data.mapper.toDomain
import com.money.manager.ex.domain.model.Account
import com.money.manager.ex.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {
    override fun getOpenAccounts(): Flow<List<Account>> {
        return accountDao.getOpenAccountsWithCurrency().map { list ->
            list.map { it.toDomain() }
        }
    }
}
