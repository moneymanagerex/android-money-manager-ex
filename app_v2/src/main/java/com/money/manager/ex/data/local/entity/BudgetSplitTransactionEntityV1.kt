package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "BUDGETSPLITTRANSACTIONS_V1",
    indices = [
        Index(value = ["TRANSID"], name = "IDX_BUDGETSPLITTRANSACTIONS_TRANSID")
    ]
)
data class BudgetSplitTransactionEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "SPLITTRANSID")
    val splitTransId: Int? = null,
    @ColumnInfo(name = "TRANSID")
    val transId: Int,
    @ColumnInfo(name = "CATEGID")
    val categId: Int?,
    @ColumnInfo(name = "SPLITTRANSAMOUNT", typeAffinity = ColumnInfo.REAL)
    val splitTransAmount: BigDecimal?,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
