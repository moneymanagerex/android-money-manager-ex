package com.money.manager.ex.data.local.pojo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.money.manager.ex.data.local.entity.AccountEntityV1
import com.money.manager.ex.data.local.entity.CurrencyFormatEntityV1
import java.math.BigDecimal

data class AccountWithBalancePojo(
    @Embedded
    val account: AccountEntityV1,

    @Relation(
        parentColumn = "CURRENCYID",
        entityColumn = "CURRENCYID"
    )
    val currency: CurrencyFormatEntityV1?,

    @ColumnInfo(name = "TOTAL")
    val total: BigDecimal,
    
    @ColumnInfo(name = "RECONCILED")
    val reconciled: BigDecimal,

    @ColumnInfo(name = "TOTALBASECONVRATE")
    val totalBaseConvRate: BigDecimal,

    @ColumnInfo(name = "RECONCILEDBASECONVRATE")
    val reconciledBaseConvRate: BigDecimal
)
