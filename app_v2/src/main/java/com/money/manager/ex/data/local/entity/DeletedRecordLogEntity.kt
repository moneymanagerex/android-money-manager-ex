package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "pb_DELETED_RECORDS_LOG",
    primaryKeys = ["table_name", "pb_id"] // <-- Chiave composta esplicita
)
data class DeletedRecordLogEntity(
    @ColumnInfo(name = "table_name")
    val tableName: String,

    @ColumnInfo(name = "pb_id")
    val pbId: String,

    @ColumnInfo(name = "deleted_at", defaultValue = "(STRFTIME('%Y-%m-%dT%H:%M:%SZ', 'NOW'))")
    val deletedAt: String? = null
)
