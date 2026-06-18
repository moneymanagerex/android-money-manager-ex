package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "BUDGETTABLE_V1",
    indices = [
        Index(value = ["BUDGETYEARID"], name = "IDX_BUDGETTABLE_BUDGETYEARID")
    ]
)
data class BudgetTableEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BUDGETENTRYID")
    val budgetEntryId: Int? = null,
    @ColumnInfo(name = "BUDGETYEARID")
    val budgetYearId: Int?,
    @ColumnInfo(name = "CATEGID")
    val categId: Int?,
    @ColumnInfo(name = "PERIOD")
    val period: String,
    @ColumnInfo(name = "AMOUNT", typeAffinity = ColumnInfo.REAL)
    val amount: BigDecimal,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "ACTIVE")
    val active: Int?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
