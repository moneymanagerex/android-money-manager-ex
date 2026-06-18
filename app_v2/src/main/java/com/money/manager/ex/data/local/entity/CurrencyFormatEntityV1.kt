package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "CURRENCYFORMATS_V1",
    indices = [
        Index(value = ["CURRENCY_SYMBOL"], name = "IDX_CURRENCYFORMATS_SYMBOL")
    ]
)
data class CurrencyFormatEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CURRENCYID")
    val currencyId: Int? = null,
    @ColumnInfo(name = "CURRENCYNAME")
    val currencyName: String,
    @ColumnInfo(name = "PFX_SYMBOL")
    val pfxSymbol: String?,
    @ColumnInfo(name = "SFX_SYMBOL")
    val sfxSymbol: String?,
    @ColumnInfo(name = "DECIMAL_POINT")
    val decimalPoint: String?,
    @ColumnInfo(name = "GROUP_SEPARATOR")
    val groupSeparator: String?,
    @ColumnInfo(name = "UNIT_NAME")
    val unitName: String?,
    @ColumnInfo(name = "CENT_NAME")
    val centName: String?,
    @ColumnInfo(name = "SCALE")
    val scale: Int?,
    @ColumnInfo(name = "BASECONVRATE", typeAffinity = ColumnInfo.REAL)
    val baseConvRate: BigDecimal?,
    @ColumnInfo(name = "CURRENCY_SYMBOL")
    val currencySymbol: String,
    @ColumnInfo(name = "CURRENCY_TYPE")
    val currencyType: String,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
