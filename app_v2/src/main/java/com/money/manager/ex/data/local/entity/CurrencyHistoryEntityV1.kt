package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "CURRENCYHISTORY_V1",
    indices = [
        Index(value = ["CURRENCYID", "CURRDATE"], name = "IDX_CURRENCYHISTORY_CURRENCYID_CURRDATE")
    ]
)
data class CurrencyHistoryEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CURRHISTID")
    val currHistId: Int? = null,
    @ColumnInfo(name = "CURRENCYID")
    val currencyId: Int,
    @ColumnInfo(name = "CURRDATE")
    val currDate: String,
    @ColumnInfo(name = "CURRVALUE", typeAffinity = ColumnInfo.REAL)
    val currValue: BigDecimal,
    @ColumnInfo(name = "CURRUPDTYPE")
    val currUpdType: Int?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
