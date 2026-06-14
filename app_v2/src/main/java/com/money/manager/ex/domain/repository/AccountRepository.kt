package com.money.manager.ex.domain.repository

import com.money.manager.ex.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getOpenAccounts(): Flow<List<Account>>
}
