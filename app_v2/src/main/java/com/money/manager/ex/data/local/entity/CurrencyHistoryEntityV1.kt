package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CURRENCYHISTORY_V1")
data class CurrencyHistoryEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CURRHISTID")
    val currHistId: Int = 0,
    @ColumnInfo(name = "CURRENCYID")
    val currencyId: Int,
    @ColumnInfo(name = "CURRDATE")
    val currDate: String,
    @ColumnInfo(name = "CURRVALUE")
    val currValue: Double,
    @ColumnInfo(name = "CURRUPDTYPE")
    val currUpdType: Int?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
