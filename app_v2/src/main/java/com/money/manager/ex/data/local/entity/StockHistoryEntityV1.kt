package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "STOCKHISTORY_V1",
    indices = [
        Index(value = ["SYMBOL"], name = "IDX_STOCKHISTORY_SYMBOL")
    ]
)
data class StockHistoryEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "HISTID")
    val histId: Int? = null,
    @ColumnInfo(name = "SYMBOL")
    val symbol: String,
    @ColumnInfo(name = "DATE")
    val date: String,
    @ColumnInfo(name = "VALUE", typeAffinity = ColumnInfo.UNDEFINED)
    val value: BigDecimal,
    @ColumnInfo(name = "UPDTYPE")
    val updType: Int?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
