package com.money.manager.ex.domain.model

data class Account(
    val id: Int,
    val name: String,
    val type: AccountType,
    val status: String,
    val isFavorite: Boolean,
    val currencySymbol: String,
    val currencyPrefix: String?,
    val balance: Double = 0.0,
    val ledgerBalance: Double = 0.0,
    val currencyCode: String = ""
)
