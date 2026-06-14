package com.money.manager.ex.data.mapper

import com.money.manager.ex.data.local.pojo.AccountWithCurrency
import com.money.manager.ex.domain.model.Account

import com.money.manager.ex.domain.model.AccountType

fun AccountWithCurrency.toDomain(): Account {
    return Account(
        id = this.account.accountId,
        name = this.account.accountName,
        type = AccountType.fromTitle(this.account.accountType) ?: AccountType.CASH,
        status = this.account.status,
        isFavorite = this.account.favoriteAcct.equals("true", ignoreCase = true),
        currencySymbol = this.currency?.currencySymbol ?: "",
        currencyPrefix = this.currency?.pfxSymbol
    )
}
