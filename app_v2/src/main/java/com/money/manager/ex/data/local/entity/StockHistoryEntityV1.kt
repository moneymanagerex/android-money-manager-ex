package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "STOCKHISTORY_V1")
data class StockHistoryEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "HISTID")
    val histId: Int = 0,
    @ColumnInfo(name = "SYMBOL")
    val symbol: String,
    @ColumnInfo(name = "DATE")
    val date: String,
    @ColumnInfo(name = "VALUE")
    val value: Double,
    @ColumnInfo(name = "UPDTYPE")
    val updType: Int?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
