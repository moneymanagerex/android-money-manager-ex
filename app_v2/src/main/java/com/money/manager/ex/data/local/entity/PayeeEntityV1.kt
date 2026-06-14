package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PAYEE_V1")
data class PayeeEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "PAYEEID")
    val payeeId: Int = 0,
    @ColumnInfo(name = "PAYEENAME")
    val payeeName: String,
    @ColumnInfo(name = "CATEGID")
    val categId: Int?,
    @ColumnInfo(name = "NUMBER")
    val number: String?,
    @ColumnInfo(name = "WEBSITE")
    val website: String?,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "ACTIVE")
    val active: Int?,
    @ColumnInfo(name = "PATTERN")
    val pattern: String? = "",
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
