package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "BUDGETYEAR_V1",
    indices = [
        Index(value = ["BUDGETYEARNAME"], name = "IDX_BUDGETYEAR_BUDGETYEARNAME")
    ]
)
data class BudgetYearEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BUDGETYEARID")
    val budgetYearId: Int? = null,
    @ColumnInfo(name = "BUDGETYEARNAME")
    val budgetYearName: String,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
