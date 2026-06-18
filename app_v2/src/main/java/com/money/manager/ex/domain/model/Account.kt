package com.money.manager.ex.domain.model

import java.math.BigDecimal

data class Account(
    val id: Int,
    val name: String,
    val type: AccountType,
    val status: String,
    val isFavorite: Boolean,
    val currencySymbol: String,
    val currencyPrefix: String?,
    val balance: BigDecimal = BigDecimal.ZERO,
    val ledgerBalance: BigDecimal = BigDecimal.ZERO,
    val currencyCode: String = ""
)
