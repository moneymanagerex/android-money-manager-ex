package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SHAREINFO_V1")
data class ShareInfoEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "SHAREINFOID")
    val shareInfoId: Int = 0,
    @ColumnInfo(name = "CHECKINGACCOUNTID")
    val checkingAccountId: Int,
    @ColumnInfo(name = "SHARENUMBER")
    val shareNumber: Double?,
    @ColumnInfo(name = "SHAREPRICE")
    val sharePrice: Double?,
    @ColumnInfo(name = "SHARECOMMISSION")
    val shareCommission: Double?,
    @ColumnInfo(name = "SHARELOT")
    val shareLot: String?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
