package com.money.manager.ex.data.repository

import com.money.manager.ex.domain.model.Account
import com.money.manager.ex.domain.model.AccountType
import com.money.manager.ex.domain.repository.AccountRepository
import java.math.BigDecimal
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

            Account(1, "Bank", AccountType.CHECKING, "Open", true, "€", null, BigDecimal.valueOf(12000.0), BigDecimal.valueOf(11500.0)),
            Account(2, "Cash", AccountType.CASH, "Open", false, "€", null, BigDecimal.valueOf(500.0), BigDecimal.valueOf(500.0)),
            Account(
                id = 3,
                name = "Carta Credito",
                type = AccountType.CREDIT_CARD,
                status = "Open",
                isFavorite = true,
                currencySymbol = "$",
                currencyPrefix = null,
                balance = BigDecimal.valueOf(-1200.0),
                ledgerBalance = BigDecimal.valueOf(-150.0)
            )
        )
        emit(mockData)
    }
}
