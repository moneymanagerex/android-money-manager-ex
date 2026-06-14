package com.money.manager.ex.domain.usecase

import com.money.manager.ex.domain.model.Account
import com.money.manager.ex.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<List<Account>> {
        return repository.getOpenAccounts()
    }
}
