package com.money.manager.ex.data.repository

import com.money.manager.ex.domain.model.Account
import com.money.manager.ex.domain.model.AccountType
import com.money.manager.ex.domain.repository.AccountRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeAccountRepositoryImpl @Inject constructor() : AccountRepository {
    override fun getOpenAccounts(): Flow<List<Account>> = flow {
        // Simuliamo un caricamento
        emit(emptyList()) 
        delay(500) 
        
        val mockData = listOf(

            Account(1, "Bank", AccountType.CHECKING, "Open", true, "€", null, 12000.0, 11500.0),
            Account(2, "Cash", AccountType.CASH, "Open", false, "€", null, 500.0, 500.0),
            Account(
                id = 3,
                name = "Carta Credito",
                type = AccountType.CREDIT_CARD,
                status = "Open",
                isFavorite = true,
                currencySymbol = "$",
                currencyPrefix = null,
                balance = -1200.0,
                ledgerBalance = -150.0
            )
        )
        emit(mockData)
    }
}
