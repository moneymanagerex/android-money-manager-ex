package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pb_DELETED_RECORDS_LOG")
data class DeletedRecordLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "table_name")
    val tableName: String,
    @ColumnInfo(name = "pb_id")
    val pbId: String,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: String?
)
