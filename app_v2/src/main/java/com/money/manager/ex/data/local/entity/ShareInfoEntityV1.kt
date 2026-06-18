package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "SHAREINFO_V1",
    indices = [
        Index(value = ["CHECKINGACCOUNTID"], name = "IDX_SHAREINFO")
    ]
)
data class ShareInfoEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "SHAREINFOID")
    val shareInfoId: Int = 0,
    @ColumnInfo(name = "CHECKINGACCOUNTID")
    val checkingAccountId: Int,
    @ColumnInfo(name = "SHARENUMBER", typeAffinity = ColumnInfo.REAL)
    val shareNumber: BigDecimal?,
    @ColumnInfo(name = "SHAREPRICE", typeAffinity = ColumnInfo.REAL)
    val sharePrice: BigDecimal?,
    @ColumnInfo(name = "SHARECOMMISSION", typeAffinity = ColumnInfo.REAL)
    val shareCommission: BigDecimal?,
    @ColumnInfo(name = "SHARELOT")
    val shareLot: String?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
