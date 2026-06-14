package com.money.manager.ex.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.money.manager.ex.data.local.entity.AccountEntityV1
import com.money.manager.ex.data.local.entity.CurrencyFormatEntityV1

data class AccountWithCurrency(
    @Embedded val account: AccountEntityV1,
    @Relation(
        parentColumn = "CURRENCYID",
        entityColumn = "CURRENCYID"
    )
    val currency: CurrencyFormatEntityV1?
)
