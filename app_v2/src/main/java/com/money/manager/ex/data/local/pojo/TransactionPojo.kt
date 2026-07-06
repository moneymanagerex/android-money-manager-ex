package com.money.manager.ex.data.local.pojo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.money.manager.ex.data.local.entity.CheckingAccountEntityV1

data class TransactionPojo(
    @Embedded
    val transaction: CheckingAccountEntityV1,
    @ColumnInfo(name = "categname")
    val categoryName: String?,
    @ColumnInfo(name = "PAYEENAME")
    val payeeName: String?,
    @ColumnInfo(name = "ACCOUNTNAME")
    val accountName: String?,
    @ColumnInfo(name = "TOACCOUNTNAME")
    val toAccountName: String?
)
