package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BUDGETYEAR_V1")
data class BudgetYearEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BUDGETYEARID")
    val budgetYearId: Int = 0,
    @ColumnInfo(name = "BUDGETYEARNAME")
    val budgetYearName: String,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
