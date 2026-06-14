package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BUDGETSPLITTRANSACTIONS_V1")
data class BudgetSplitTransactionEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "SPLITTRANSID")
    val splitTransId: Int = 0,
    @ColumnInfo(name = "TRANSID")
    val transId: Int,
    @ColumnInfo(name = "CATEGID")
    val categId: Int?,
    @ColumnInfo(name = "SPLITTRANSAMOUNT")
    val splitTransAmount: Double?,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
