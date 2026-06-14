package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "INFOTABLE_V1")
data class InfoTableEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "INFOID")
    val infoId: Int = 0,
    @ColumnInfo(name = "INFONAME")
    val infoName: String,
    @ColumnInfo(name = "INFOVALUE")
    val infoValue: String,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
