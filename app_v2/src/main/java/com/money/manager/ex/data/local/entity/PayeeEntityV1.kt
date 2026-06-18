package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "PAYEE_V1",
    indices = [
        Index(value = ["PAYEENAME"], name = "IDX_PAYEE_INFONAME")
    ]
)
data class PayeeEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "PAYEEID")
    val payeeId: Int? = null,
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
    @ColumnInfo(name = "PATTERN", defaultValue = "''")
    val pattern: String? = "",
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0

)
