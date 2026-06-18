package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "ASSETS_V1",
    indices = [
        Index(value = ["ASSETTYPE"], name = "IDX_ASSETS_ASSETTYPE")
    ]
)
data class AssetEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ASSETID")
    val assetId: Int? = null,
    @ColumnInfo(name = "STARTDATE")
    val startDate: String,
    @ColumnInfo(name = "ASSETNAME")
    val assetName: String,
    @ColumnInfo(name = "ASSETSTATUS")
    val assetStatus: String?,
    @ColumnInfo(name = "CURRENCYID")
    val currencyId: Int?,
    @ColumnInfo(name = "VALUECHANGEMODE")
    val valueChangeMode: String?,
    @ColumnInfo(name = "VALUE", typeAffinity = ColumnInfo.REAL)
    val value: BigDecimal?,
    @ColumnInfo(name = "VALUECHANGE")
    val valueChange: String?,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "VALUECHANGERATE", typeAffinity = ColumnInfo.REAL)
    val valueChangeRate: BigDecimal?,
    @ColumnInfo(name = "ASSETTYPE")
    val assetType: String?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
