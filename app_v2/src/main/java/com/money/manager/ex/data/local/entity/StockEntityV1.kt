package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "STOCK_V1")
data class StockEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "STOCKID")
    val stockId: Int = 0,
    @ColumnInfo(name = "HELDAT")
    val heldAt: Int?,
    @ColumnInfo(name = "PURCHASEDATE")
    val purchaseDate: String,
    @ColumnInfo(name = "STOCKNAME")
    val stockName: String,
    @ColumnInfo(name = "SYMBOL")
    val symbol: String?,
    @ColumnInfo(name = "NUMSHARES")
    val numShares: Double?,
    @ColumnInfo(name = "PURCHASEPRICE")
    val purchasePrice: Double,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "CURRENTPRICE")
    val currentPrice: Double,
    @ColumnInfo(name = "VALUE")
    val value: Double?,
    @ColumnInfo(name = "COMMISSION")
    val commission: Double?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
