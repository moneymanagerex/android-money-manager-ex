package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TRANSLINK_V1",
    indices = [
        Index(value = ["LINKTYPE", "LINKRECORDID"], name = "IDX_LINKRECORD"),
        Index(value = ["CHECKINGACCOUNTID"], name = "IDX_CHECKINGACCOUNT")
    ]
)
data class TransLinkEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TRANSLINKID")
    val transLinkId: Int = 0,
    @ColumnInfo(name = "CHECKINGACCOUNTID")
    val checkingAccountId: Int,
    @ColumnInfo(name = "LINKTYPE")
    val linkType: String,
    @ColumnInfo(name = "LINKRECORDID")
    val linkRecordId: Int,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
