package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TRANSLINK_V1")
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
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
