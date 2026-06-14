package com.money.manager.ex.data.local.pojo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.money.manager.ex.data.local.entity.AccountEntityV1
import com.money.manager.ex.data.local.entity.CurrencyFormatEntityV1

data class AccountWithBalancePojo(
    @Embedded
    val account: AccountEntityV1,

    @Relation(
        parentColumn = "CURRENCYID",
        entityColumn = "CURRENCYID"
    )
    val currency: CurrencyFormatEntityV1?,

    @ColumnInfo(name = "TOTAL")
    val total: Double,
    
    @ColumnInfo(name = "RECONCILED")
    val reconciled: Double,

    @ColumnInfo(name = "TOTALBASECONVRATE")
    val totalBaseConvRate: Double,

    @ColumnInfo(name = "RECONCILEDBASECONVRATE")
    val reconciledBaseConvRate: Double
)
