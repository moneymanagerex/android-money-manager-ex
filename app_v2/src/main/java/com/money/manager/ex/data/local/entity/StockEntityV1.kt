package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "STOCK_V1",
    indices = [
        Index(value = ["HELDAT"], name = "IDX_STOCK_HELDAT")
    ]
)
data class StockEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "STOCKID")
    val stockId: Int? = null,
    @ColumnInfo(name = "HELDAT")
    val heldAt: Int?,
    @ColumnInfo(name = "PURCHASEDATE")
    val purchaseDate: String,
    @ColumnInfo(name = "STOCKNAME")
    val stockName: String,
    @ColumnInfo(name = "SYMBOL")
    val symbol: String?,
    @ColumnInfo(name = "NUMSHARES", typeAffinity = ColumnInfo.REAL)
    val numShares: BigDecimal?,
    @ColumnInfo(name = "PURCHASEPRICE", typeAffinity = ColumnInfo.REAL)
    val purchasePrice: BigDecimal,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "CURRENTPRICE", typeAffinity = ColumnInfo.REAL)
    val currentPrice: BigDecimal,
    @ColumnInfo(name = "VALUE", typeAffinity = ColumnInfo.REAL)
    val value: BigDecimal?,
    @ColumnInfo(name = "COMMISSION", typeAffinity = ColumnInfo.REAL)
    val commission: BigDecimal?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
